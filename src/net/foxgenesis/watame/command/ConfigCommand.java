package net.foxgenesis.watame.command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.foxgenesis.config.fields.JSONObjectAdv;
import net.foxgenesis.watame.WatameBot;

/**
 * Slash command to manually configure database values
 * 
 * @author Ashley
 *
 */
public class ConfigCommand extends ListenerAdapter {

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
				break;
			}

			case "config-set" -> {
				event.deferReply(true).queue();
				InteractionHook hook = event.getHook();

				OptionType type = event.getOption("type", t -> OptionType.valueOf(t.getAsString()));
				String value = event.getOption(type.name().toLowerCase(), null, OptionMapping::getAsString);
				String key = event.getOption("key", OptionMapping::getAsString);
				JSONObjectAdv config = getConfig(event.getGuild());
				logger.debug("Config-set t:{} k:{} v:{} [{}]", type, key, value, event.getOptions());

				if (value == null) {
					config.remove(key);
					logger.info("{}[{}] Removed {} from the configuration", user.getName(), user.getId(), key);
					hook.editOriginal("Deleted " + key).queue();
				} else {
					config.put(key, value);
					logger.info("{}[{}] Put {} -> {} into the configuration", user.getName(), user.getId(), key, value);
					hook.editOriginal("Put " + value + " in " + key).queue();
				}
				break;
			}
			}
		}
	}

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		switch (event.getCommandPath()) {
		case "config-set" -> {
			switch (event.getFocusedOption().getName()) {
			case "type" -> {
				// only display words that start with the user's current input
				List<Command.Choice> options = Stream.of(OptionType.values())
						.filter(word -> word.name().contains(event.getFocusedOption().getValue()))
						.map(word -> new Command.Choice(word.name(), word.name())) // map the words to choices
						.collect(Collectors.toList());
				event.replyChoices(options).queue();
			}
			}
		}
		}
	}

	private static JSONObjectAdv getConfig(Guild guild) {
		return WatameBot.getInstance().getDataForGuild(guild).getConfig();
	}
}
