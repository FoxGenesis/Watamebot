package net.foxgenesis.database;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.foxgenesis.watame.plugin.Plugin;

public interface IDatabaseManager extends AutoCloseable {
	/**
	 * Check if all guild data has been processed and is ready for use.
	 * 
	 * @return Returns {@code true} when all data has been loaded from the database
	 * @see #isConnectionValid()
	 */
	public boolean isReady();

	boolean register(@Nonnull Plugin owningPlugin, @Nonnull AbstractDatabase database) throws IOException;

	boolean isDatabaseRegistered(@Nonnull AbstractDatabase database);
	
	@Nonnull
	String getName();
}
