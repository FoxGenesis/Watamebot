package net.foxgenesis.watame.command;

import java.time.Duration;
import java.time.OffsetDateTime;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PingCommand extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getName().equals("ping")) {
			event.reply(
					"Pong! ("
							+ "%,.0fms".formatted(
									(double) Duration.between(event.getTimeCreated(), OffsetDateTime.now()).toMillis())
							+ ")")
					.setEphemeral(true).queue();
		}
	}
}
