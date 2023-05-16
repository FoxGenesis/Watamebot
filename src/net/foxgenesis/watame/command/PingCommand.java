package net.foxgenesis.watame.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PingCommand extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getName().equals("ping")) {
			event.deferReply(true).flatMap(e -> e.getJDA().getRestPing())
					.queue(time -> event.getHook().editOriginal("Pong! (" + "%,.0dms".formatted(time) + ")").queue());
		}
	}
}
