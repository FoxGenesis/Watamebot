package net.foxgenesis.watame.command;

import static net.foxgenesis.watame.util.Colors.ERROR;
import static net.foxgenesis.watame.util.Colors.INFO;
import static net.foxgenesis.watame.util.Colors.NOTICE;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyMapping;
import net.foxgenesis.watame.property.PluginPropertyProvider;
import net.foxgenesis.watame.util.Response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/**
 * Slash command to manually configure database values
 * 
 * @author Ashley
 *
 */
public class ConfigCommand extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger("Configuration Command");

	private static final String DEFAULT = "Default";
	private static final String CONFIG_VALUE_FORMAT = "* %s = `%s`";

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.isFromGuild()) {
			switch (event.getName()) {
				case "options" -> handleOptions(event);
			}
		}
	}

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		AutoCompleteQuery option = event.getFocusedOption();
		Guild guild = event.getGuild();
		if (event.isFromGuild() && guild != null) {
			if (event.getFullCommandName().startsWith("options configuration") && option.getName().equals("key")) {
				PluginPropertyProvider provider = WatameBot.INSTANCE.getPropertyProvider();
				String value = option.getValue().toLowerCase();

				@SuppressWarnings("null") List<Command.Choice> choices = provider.getPropertyList().stream()
						.filter(PropertyInfo::modifiable).filter(info -> info.name().toLowerCase().contains(value))
						.map(info -> new Command.Choice(info.category() + " " + info.name(), info.id())).limit(25)
						.toList();
				event.replyChoices(choices).queue();
			}
		}
	}

	/**
	 * Handle the {@code options} slash command.
	 * 
	 * @param event - slash command event
	 */
	private static void handleOptions(SlashCommandInteractionEvent event) {
		String group = Objects.requireNonNull(event.getSubcommandGroup());
		switch (group) {
			case "configuration" -> handleConfiguration(event);
		}
	}

	/**
	 * Handle the {@code options/configuration} slash command.
	 * 
	 * @param event - slash command event
	 */
	private static void handleConfiguration(SlashCommandInteractionEvent event) {
		PluginPropertyProvider provider = WatameBot.INSTANCE.getPropertyProvider();
		String sub = Objects.requireNonNull(event.getSubcommandName());
		InteractionHook hook = event.getHook();

		int tmp = sub.indexOf('-');
		if (tmp != -1) {
			sub = sub.substring(0, tmp);
		}

		switch (sub) {
			case "get" -> getConfigurationSetting(event, provider, hook);
			case "set" -> setConfigurationSetting(event, provider, hook);
			case "remove" -> removeConfigurationSetting(event, provider, hook);
			case "list" -> listAllConfigurationSettings(event, provider, hook);
		}
	}

	/**
	 * Get a property from guild settings.
	 * 
	 * @param event    - slash command event
	 * @param provider - property provider
	 * @param hook     - event hook
	 */
	private static void getConfigurationSetting(SlashCommandInteractionEvent event, PluginPropertyProvider provider,
			InteractionHook hook) {
		event.deferReply(true).queue();

		// Make sure key is valid
		ensureKey(event.getOption("key", OptionMapping::getAsInt), provider, hook, property -> {
			User user = event.getUser();
			logger.info("{}[{}] Checked configuration for {}", user.getName(), user.getId(), property.getInfo());

			String pInfo = displayPropertyString(property.getInfo());
			String pValue = getUserFriendlyValue(property.get(event.getGuild()));

			hook.editOriginalEmbeds(
					Response.info("\u2699 Configuration \u2699", CONFIG_VALUE_FORMAT.formatted(pInfo, pValue))).queue();
		});
	}

	/**
	 * Set a property in the guild settings.
	 * 
	 * @param event    - slash command event
	 * @param provider - property provider
	 * @param hook     - event hook
	 */
	private static void setConfigurationSetting(SlashCommandInteractionEvent event, PluginPropertyProvider provider,
			InteractionHook hook) {
		event.deferReply(true).queue();

		// Make sure key is valid
		ensureKey(event.getOption("key", OptionMapping::getAsInt), provider, hook,
				// Check if value is valid
				property -> ensureValue(event.getOptions(), hook, value -> {
					// Check if property is editable
					if (property.getInfo().modifiable()) {
						Member member = Objects.requireNonNull(event.getMember());
						Guild guild = member.getGuild();
						String key = displayPropertyString(property.getInfo());
						String old = getUserFriendlyValue(property.get(guild));

						boolean wasSet = switch (value.getType()) {
							default -> throw new IllegalArgumentException("Unexpected value: " + value.getType());
							case BOOLEAN -> property.set(guild, value.getAsBoolean());
							case CHANNEL -> property.set(guild, value.getAsChannel().getIdLong());
							case INTEGER -> property.set(guild, value.getAsInt());
							case ROLE, USER, MENTIONABLE -> property.set(guild, value.getAsMentionable().getIdLong());
							case NUMBER -> property.set(guild, value.getAsLong());
							case STRING -> property.set(guild, value.getAsString());
						};
						// Attempt to set the property
						if (wasSet) {
							logger.info("{}[{}] Put {} -> {} into the configuration", member.getUser().getName(),
									member.getUser().getId(), key, value.getAsString());
							hook.editOriginalEmbeds(
									Response.success("Set `%s` to `%s`".formatted(key, value.getAsString()))).queue();
							logChange(member, key, old, value.getAsString());
						} else
							unknownError(hook);
					} else
						hook.editOriginalEmbeds(response(ERROR, "Error", "Property is not user editable")).queue();
				}));
	}

	/**
	 * Remove a property in the guild settings.
	 * 
	 * @param event    - slash command event
	 * @param provider - property provider
	 * @param hook     - event hook
	 */
	private static void removeConfigurationSetting(SlashCommandInteractionEvent event, PluginPropertyProvider provider,
			InteractionHook hook) {
		event.deferReply(true).queue();

		// Make sure key is valid
		ensureKey(event.getOption("key", OptionMapping::getAsInt), provider, hook, property -> {
			// Check if property is editable
			if (property.getInfo().modifiable()) {
				Member member = Objects.requireNonNull(event.getMember());
				Guild guild = member.getGuild();
				String key = displayPropertyString(property.getInfo());
				String old = getUserFriendlyValue(property.get(guild));

				// Attempt to set the property
				if (property.remove(guild)) {
					logger.info("{}[{}] Removed {} from the configuration", member.getUser().getName(),
							member.getUser().getId(), property.getInfo());
					hook.editOriginalEmbeds(Response.success("Set `%s` to `%s`".formatted(key, DEFAULT))).queue();
					logChange(member, key, old, DEFAULT);
				} else
					unknownError(hook);
			} else
				hook.editOriginalEmbeds(response(ERROR, "Error", "Property is not user editable")).queue();
		});
	}

	/**
	 * List all property values for the guild.
	 * 
	 * @param event    - slash command event
	 * @param provider - property provider
	 * @param hook     - event hook
	 */
	private static void listAllConfigurationSettings(SlashCommandInteractionEvent event,
			PluginPropertyProvider provider, InteractionHook hook) {
		event.deferReply(true).queue();

		Guild guild = event.getGuild();

		if (guild != null) {
			// Collect all values
			HashMap<String, String> map = new HashMap<>();
			provider.getPropertyList().stream().map(info -> provider.getProperty(info)).forEach(property -> map
					.put(displayPropertyString(property.getInfo()), getUserFriendlyValue(property.get(guild))));

			// Build output
			StringBuilder builder = new StringBuilder();
			map.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEachOrdered(entry -> {
				builder.append((CONFIG_VALUE_FORMAT + "\n").formatted(entry.getKey(), entry.getValue()));
			});

			// Send
			hook.editOriginalEmbeds(response(INFO, "\u2699 Configuration List \u2699", builder.toString())).queue();
		} else
			unknownError(hook);
	}

	/**
	 * Ensure that a key is not null and is present inside the property provider.
	 * 
	 * @param key      - key to check
	 * @param provider - property provider
	 * @param hook     - event hook
	 * @param consumer - resolved property consumer
	 */
	private static void ensureKey(int id, PluginPropertyProvider provider, InteractionHook hook,
			@NotNull Consumer<PluginProperty> consumer) {
		if (id > 0) {
			try {
				consumer.accept(provider.getPropertyInfoByID(id));
			} catch (NoSuchElementException e) {
				hook.editOriginalEmbeds(Response.error("No property for id: " + id)).queue();
			}
		} else
			hook.editOriginalEmbeds(Response.error("Invalid ID [" + id + "]. ID must be greater than 0.")).queue();
	}

	/**
	 * Find the value option from event options.
	 * 
	 * @param mappings - event options
	 * @param hook     - interaction hook
	 * @param consumer - found value option
	 */
	private static void ensureValue(@NotNull List<OptionMapping> mappings, @NotNull InteractionHook hook,
			@NotNull Consumer<OptionMapping> consumer) {
		for (OptionMapping m : mappings)
			if (!m.getName().equals("key")) {
				consumer.accept(m);
				return;
			}
		hook.editOriginalEmbeds(response(ERROR, "Error", "Unable to get value field")).queue();
	}

	/**
	 * Create a command response embed.
	 * 
	 * @param color - embed color
	 * @param title - embed title
	 * @param desc  - embed description
	 * 
	 * @return Returns the created {@link MessageEmbed}
	 */
	private static MessageEmbed response(int color, @NotNull String title, @Nullable String desc) {
		return new EmbedBuilder().setColor(color).setTitle(title).setDescription(desc).build();
	}

	/**
	 * Log configuration changes to the guild's log channel.
	 * 
	 * @param user     - {@link Member} that made the change
	 * @param key      - the property key
	 * @param oldValue - old property value
	 * @param value    - new property value
	 */
	private static void logChange(Member user, String key, String oldValue, String value) {
		GuildMessageChannel channel = WatameBot.INSTANCE.getLoggingChannel().get(user.getGuild(),
				PluginPropertyMapping::getAsMessageChannel);

		if (channel != null) {
			logger.debug("Logging configuration change to {}", channel);
			channel.sendMessageEmbeds(new EmbedBuilder().setColor(NOTICE).setTitle("Configuration Change")
					.setDescription("Plugin configuration has been updated")
					.addField("Type", value == null ? "Remove" : "Update", true)
					.addField("User", user.getAsMention(), true).addField("Key", key, true)
					.addField("Old Value", oldValue, true).addField("Value", value != null ? value : "N/A", true)
					.build()).addCheck(channel::canTalk)
					.addCheck(() -> user.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)).queue();
		}
	}

	private static String displayPropertyString(PropertyInfo property) {
		return "[" + property.category() + "] " + property.name();
	}

	private static void unknownError(InteractionHook hook) {
		hook.editOriginalEmbeds(response(ERROR, "Error", "Something went wrong. Please try again later")).queue();
	}

	private static String getUserFriendlyValue(Optional<PluginPropertyMapping> value) {
		return value.map(property -> {
			if (property.isUserReadable())
				return property.getAsPlainText();
			return "Object[" + property.getLength() + "B]";
		}).orElse(DEFAULT);
	}
}
