package net.foxgenesis.watame.sql;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;

public interface IDatabaseManager {
	/**
	 * Check if the database is connected and is ready for operations.
	 * 
	 * @return Returns {@code true} if is connected and ready
	 * @see #registerStatement(String, String)
	 * @see #hasStatement(String)
	 */
	public boolean isConnectionValid();

	/**
	 * Check if all guild data has been processed and is ready for use.
	 * 
	 * @return Returns {@code true} when all data has been loaded from the database
	 * @see #isConnectionValid()
	 */
	public boolean isReady();

	/**
	 * NEED_JAVADOC
	 * @param guild
	 * @return
	 * @throws NullPointerException if {@code guild} is null
	 */
	public IGuildData getDataForGuild(@Nonnull Guild guild);
}
