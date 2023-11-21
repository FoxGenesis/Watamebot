package net.foxgenesis.watame.command;

import java.lang.management.ManagementFactory;

import net.foxgenesis.watame.util.Response;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UptimeCommand extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getName().equals("uptime"))
			event.replyEmbeds(Response.info(DurationFormatUtils
					.formatDuration(ManagementFactory.getRuntimeMXBean().getUptime(), "DD:HH:MM:SS", true)))
					.setEphemeral(true).queue();
	}
}
