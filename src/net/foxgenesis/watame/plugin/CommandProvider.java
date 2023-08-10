package net.foxgenesis.watame.plugin;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface CommandProvider {
	/**
	 * Register all {@link CommandData} that this plugin provides.
	 * <p>
	 * <b>** {@code providesCommands} must be set to true in
	 * {@code plugin.properties}! **</b>
	 * </p>
	 * 
	 * @return Returns a non-null {@link Collection} of {@link CommandData} that
	 *         this {@link Plugin} provides
	 */
	@NotNull
	Collection<CommandData> getCommands();
}
