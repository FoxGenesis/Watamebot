package net.foxgenesis.watame.plugin.require;

import java.util.Collection;

import net.foxgenesis.watame.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface CommandProvider {
	/**
	 * Register all {@link CommandData} that this plugin provides.
	 *
	 * @return Returns a non-null {@link Collection} of {@link CommandData} that
	 *         this {@link Plugin} provides
	 */
	@NotNull
	Collection<CommandData> getCommands();
}
