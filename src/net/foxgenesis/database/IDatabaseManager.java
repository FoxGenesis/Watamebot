package net.foxgenesis.database;

import java.io.IOException;

import net.foxgenesis.watame.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

public interface IDatabaseManager {
	/**
	 * Check if all guild data has been processed and is ready for use.
	 *
	 * @return Returns {@code true} when all data has been loaded from the database
	 */
	boolean isReady();

	/**
	 * Register an {@link AbstractDatabase} that a {@link Plugin} requires.
	 * 
	 * @param owningPlugin - owner of the instance
	 * @param database     - database to register
	 * 
	 * @return Returns {@code true} if the database was registered. {@code false}
	 *         otherwise.
	 * 
	 * @throws IOException Thrown if there was an error while reading database setup
	 *                     files
	 */
	boolean register(@NotNull Plugin owningPlugin, @NotNull AbstractDatabase database) throws IOException;

	/**
	 * Check if an {@link AbstractDatabase} is registered in this manager.
	 * 
	 * @param database - the database to check if it exists
	 * 
	 * @return Returns {@code true} if the database is registered, {@code false}
	 *         otherwise.
	 */
	boolean isDatabaseRegistered(@NotNull AbstractDatabase database);

	/**
	 * Get the name of this instance.
	 * 
	 * @return Returns the name of this instance.
	 */
	@NotNull
	String getName();
}
