package net.foxgenesis.watame.command;

import static net.foxgenesis.watame.Constants.Colors.ERROR;
import static net.foxgenesis.watame.Constants.Colors.INFO;
import static net.foxgenesis.watame.Constants.Colors.SUCCESS;
import static net.foxgenesis.watame.Constants.Colors.WARNING_DARK;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.foxgenesis.property.IProperty;
import net.foxgenesis.property.IPropertyProvider;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.property.IGuildPropertyMapping;

/**
 * Slash command to manually configure database values
 * 
 * @author Ashley
 *
 */
public class ConfigCommand extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger("Configuration Command");

	private static final String DEFAULT = "Default";
	private static final String CONFIG_VALUE_FORMAT = "%s = `%s`";

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
			if (event.getCommandPath().startsWith("options/configuration") && option.getName().equals("key")) {
				IPropertyProvider<String, Guild, IGuildPropertyMapping> provider = WatameBot.INSTANCE
						.getPropertyProvider();
				event.replyChoices(provider.keySet().stream().filter(key -> provider.getProperty(key).isEditable())
						.filter(key -> key.contains(option.getValue().toLowerCase()))
						.map(key -> new Command.Choice(key, key)).toList()).queue();
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
		IPropertyProvider<String, Guild, IGuildPropertyMapping> provider = WatameBot.INSTANCE.getPropertyProvider();
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
	private static void getConfigurationSetting(SlashCommandInteractionEvent event,
			IPropertyProvider<String, Guild, IGuildPropertyMapping> provider, InteractionHook hook) {
		event.deferReply(true).queue();

		// Make sure key is valid
		ensureKey(event.getOption("key", OptionMapping::getAsString), provider, hook, (key, property) -> {
			User user = event.getUser();
			logger.info("{}[{}] Checked configuration for {}", user.getName(), user.getId(), key);
			hook.editOriginalEmbeds(response(INFO, "\u2699 Configuration \u2699",
					CONFIG_VALUE_FORMAT.formatted(key, provider.getProperty(Objects.requireNonNull(key)).get(
							Objects.requireNonNull(event.getGuild()), DEFAULT, IGuildPropertyMapping::getAsString))))
					.queue();
		});
	}

	/**
	 * Set a property in the guild settings.
	 * 
	 * @param event    - slash command event
	 * @param provider - property provider
	 * @param hook     - event hook
	 */
	private static void setConfigurationSetting(SlashCommandInteractionEvent event,
			IPropertyProvider<String, Guild, IGuildPropertyMapping> provider, InteractionHook hook) {
		event.deferReply(true).queue();

		// Make sure key is valid
		ensureKey(event.getOption("key", OptionMapping::getAsString), provider, hook,
				// Check if value is valid
				(key, property) -> ensureValue(event.getOptions(), hook, value -> {
					// Check if property is editable
					if (property.isEditable()) {
						Member member = Objects.requireNonNull(event.getMember());
						Guild guild = member.getGuild();
						String old = property.get(guild, DEFAULT, IGuildPropertyMapping::getAsString);

						// Attempt to set the property
						if (property.set(guild, value, true)) {
							logger.info("{}[{}] Put {} -> {} into the configuration", member.getUser().getName(),
									member.getUser().getId(), key, value);
							hook.editOriginalEmbeds(
									response(SUCCESS, "Updated", "Set `%s` to `%s`".formatted(key, value))).queue();
							logChange(member, key, old, value);
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
	private static void removeConfigurationSetting(SlashCommandInteractionEvent event,
			IPropertyProvider<String, Guild, IGuildPropertyMapping> provider, InteractionHook hook) {
		event.deferReply(true).queue();

		// Make sure key is valid
		ensureKey(event.getOption("key", OptionMapping::getAsString), provider, hook, (key, property) -> {
			// Check if property is editable
			if (property.isEditable()) {
				Member member = Objects.requireNonNull(event.getMember());
				Guild guild = member.getGuild();
				String old = property.get(guild, DEFAULT, IGuildPropertyMapping::getAsString);

				// Attempt to set the property
				if (property.set(guild, null, true)) {
					logger.info("{}[{}] Removed {} from the configuration", member.getUser().getName(),
							member.getUser().getId(), key);
					hook.editOriginalEmbeds(response(SUCCESS, "Updated", "Set `%s` to `%s`".formatted(key, DEFAULT)))
							.queue();
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
			IPropertyProvider<String, Guild, IGuildPropertyMapping> provider, InteractionHook hook) {
		event.deferReply(true).queue();

		Guild guild = event.getGuild();

		if (guild != null) {
			// Collect all values
			HashMap<String, String> map = new HashMap<>();
			provider.keySet().stream().forEach(key -> map.put(key,
					provider.getProperty(key).get(guild, DEFAULT, IGuildPropertyMapping::getAsString)));

			// Build output
			StringBuilder builder = new StringBuilder();
			map.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey())).forEachOrdered(entry -> {
				builder.append((" \u2022 " + CONFIG_VALUE_FORMAT + "\n").formatted(entry.getKey(), entry.getValue()));
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
	 * 
	 * @return Returns {@code true} if the key is not null and the property is
	 *         present in the provider
	 */
	private static void ensureKey(String key, IPropertyProvider<String, Guild, IGuildPropertyMapping> provider,
			InteractionHook hook,
			@NotNull BiConsumer<String, IProperty<String, Guild, IGuildPropertyMapping>> consumer) {
		if (key != null)
			if (provider.isPropertyPresent(key))
				consumer.accept(key, provider.getProperty(key));
			else
				hook.editOriginalEmbeds(response(ERROR, "Error", "Unkown Key")).queue();
		else
			hook.editOriginalEmbeds(response(ERROR, "Error", "Please enter a key")).queue();
	}

	/**
	 * Find the value option from event options.
	 * 
	 * @param mappings - event options
	 * @param hook     - interaction hook
	 * @param consumer - found value option
	 */
	private static void ensureValue(@NotNull List<OptionMapping> mappings, @NotNull InteractionHook hook,
			@NotNull Consumer<String> consumer) {
		for (OptionMapping m : mappings)
			if (!m.getName().equals("key")) {
				consumer.accept(m.getAsString());
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
		TextChannel channel = WatameBot.INSTANCE.getGuildLoggingChannel().get(user.getGuild(),
				IGuildPropertyMapping::getAsTextChannel);

		if (channel != null) {
			channel.sendMessageEmbeds(new EmbedBuilder().setColor(WARNING_DARK).setTitle("Configuration Change")
					.setDescription("Plugin configuration has been updated")
					.addField("Type", value == null ? "Remove" : "Update", true)
					.addField("User", user.getAsMention(), true).addField("Key", key, true)
					.addField("Old Value", oldValue, true).addField("Value", value != null ? value : "N/A", true)
					.build()).addCheck(channel::canTalk)
					.addCheck(() -> user.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)).queue();
		}
	}

	private static void unknownError(InteractionHook hook) {
		hook.editOriginalEmbeds(response(ERROR, "Error", "Something went wrong. Please try again later")).queue();
	}
}
