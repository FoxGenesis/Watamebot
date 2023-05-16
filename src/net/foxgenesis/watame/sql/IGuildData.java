package net.foxgenesis.watame.sql;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

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
	 * Get the guild id this instance represents
	 * 
	 * @return Returns the guld id as a long
	 */
	public long getGuildID();

	/**
	 * Get the guild JSON configuration.
	 * 
	 * @return An extension of a {@link JSONObject} with a set function for updates
	 *         and removals or {@code null} if no database data has been passed
	 */
	@SuppressWarnings({ "removal", "deprecation" })
	public JSONObjectAdv getConfig();
}
