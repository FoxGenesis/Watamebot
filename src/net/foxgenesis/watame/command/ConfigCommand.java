package net.foxgenesis.watame.command;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.foxgenesis.config.fields.JSONObjectAdv;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.property.IGuildPropertyMapping;

/**
 * Slash command to manually configure database values
 * 
 * @author Ashley
 *
 */
public class ConfigCommand extends ListenerAdapter {

	public static final List<Command.Choice> options = Collections.unmodifiableList(Stream.of(OptionType.values())
			.filter(type -> !(type == OptionType.UNKNOWN || type == OptionType.SUB_COMMAND
					|| type == OptionType.SUB_COMMAND_GROUP))
			.map(word -> new Command.Choice(word.name().toLowerCase(), word.name())).collect(Collectors.toList()));

	private static final Logger logger = LoggerFactory.getLogger("Configuration Command");

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.isFromGuild()) {
			User user = event.getUser();

			switch (event.getCommandPath()) {

			case "config-get" -> {
				event.deferReply(true).queue();
				InteractionHook hook = event.getHook();

				String key = event.getOption("key", OptionMapping::getAsString);

				logger.info("{}[{}] Checked configuration for {}", user.getName(), user.getId(), key);
				hook.editOriginal("Value: " + getConfig(event.getGuild()).optString(key, "null/using default")).queue();
			}
			case "config-set" -> {
				event.deferReply(true).queue();
				InteractionHook hook = event.getHook();

				OptionType type = event.getOption("type", t -> OptionType.valueOf(t.getAsString().toUpperCase()));

				if (type != null) {
					String value = event.getOption(type.name().toLowerCase(), null, OptionMapping::getAsString);
					String key = event.getOption("key", OptionMapping::getAsString);
					logger.debug("Config-set t:{} k:{} v:{} [{}]", type, key, value, event.getOptions());

					updateConfig(hook, event.getGuild(), user, key, value);
				} else
					hook.editOriginal("Type is not valid!").queue();
			}
			}
		}
	}

	@SuppressWarnings({ "removal", "deprecation" })
	private static void updateConfig(@Nonnull InteractionHook hook, Guild guild, @Nonnull User user, String key,
			@Nullable String value) {
		JSONObjectAdv config = getConfig(guild);

		if (value == null) {
			config.remove(key);
			logger.info("{}[{}] Removed {} from the configuration", user.getName(), user.getId(), key);
			hook.editOriginal("Deleted " + key).queue();
		} else {
			config.put(key, value);
			logger.info("{}[{}] Put {} -> {} into the configuration", user.getName(), user.getId(), key, value);
			hook.editOriginal("Put " + value + " in " + key).queue();
		}

		TextChannel channel = WatameBot.getInstance().getGuildLoggingChannel().get(guild,
				IGuildPropertyMapping::getAsTextChannel);

		if (channel != null) {
			channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.orange).setTitle("Configuration Change")
					.addField("User", user.getAsMention(), true).addField("Key", key, true)
					.addField("Value", value != null ? value : "N/A", true)
					.addField("Type", value == null ? "Remove" : "Update", false).build()).addCheck(channel::canTalk)
					.queue();
		}
	}

	@SuppressWarnings("removal")
	private static JSONObjectAdv getConfig(Guild guild) {
		return WatameBot.getInstance().getDataForGuild(guild).getConfig();
	}
}
