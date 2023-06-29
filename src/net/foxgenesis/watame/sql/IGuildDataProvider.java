package net.foxgenesis.watame.sql;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Guild;

public interface IGuildDataProvider {
	/**
	 * Check if all guild data has been processed and is ready for use.
	 * 
	 * @return Returns {@code true} when all data has been loaded from the database
	 */
	public boolean isReady();

	/**
	 * NEED_JAVADOC
	 * 
	 * @param guild
	 * 
	 * @return
	 * 
	 * @throws NullPointerException if {@code guild} is null
	 */
	public IGuildData getDataForGuild(@NotNull Guild guild);
}
