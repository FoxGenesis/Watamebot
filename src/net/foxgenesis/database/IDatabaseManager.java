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

	boolean register(@NotNull Plugin owningPlugin, @NotNull AbstractDatabase database) throws IOException;

	boolean isDatabaseRegistered(@NotNull AbstractDatabase database);

	@NotNull
	String getName();
}
