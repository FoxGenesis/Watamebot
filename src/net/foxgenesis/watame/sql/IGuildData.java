package net.foxgenesis.watame.sql;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.config.fields.JSONObjectAdv;

/**
 * Interface containing methods used to access {@link GuildData} instances.
 * 
 * @author Ashley
 *
 */
public interface IGuildData {
	/**
	 * Get the temporary data store for this instance.
	 * 
	 * @return A concurrent map containing temporary data
	 */
	public ConcurrentHashMap<String, Object> getTempData();

	/**
	 * Get the channel designated as the log channel.
	 * 
	 * @return A {@link Channel} as designated or {@code null} if none was set
	 */
	public Channel getLogChannel();

	/**
	 * Get the {@link Guild} represented by this instance.
	 * 
	 * @return The guild used
	 */
	public Guild getGuild();

	/**
	 * Get the guild JSON configuration.
	 * 
	 * @return An extension of a {@link JSONObject} with a set function for updates
	 *         and removals or {@code null} if no database data has been passed
	 */
	public JSONObjectAdv getConfig();
}
