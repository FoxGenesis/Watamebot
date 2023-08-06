package net.foxgenesis.watame.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import net.foxgenesis.config.fields.JSONObjectAdv;
import net.foxgenesis.util.function.QuadFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Class used to contain guild database data.
 * 
 * @author Ashley
 *
 */
public class GuildData implements IGuildData {

	/**
	 * Link to parent data manager
	 */
	@NotNull
	private final QuadFunction<String, Object, Long, Boolean, Integer> consumer;

	/**
	 * {@link Guild} this instance is based on
	 */

	// private final Guild guild;
	private final long id;

	/**
	 * Temporary storage
	 */
	private final ConcurrentHashMap<String, Object> temp = new ConcurrentHashMap<>();

	/**
	 * JSON object used to store guild settings
	 */
	@SuppressWarnings({ "removal", "deprecation" })
	private JSONObjectAdv data;

	volatile boolean setup = false;

	/**
	 * Creates a new instance of {@link GuildData} referencing the supplied guild id
	 * and data update consumer. <blockquote><b> *** THIS SHOULD ONLY BE CALLED VIA
	 * {@link WatameBotDatabase}! *** </blockquote></b>
	 * 
	 * @param id       - The guild id that this instance represents
	 * @param consumer - guild data update consumer
	 */
	GuildData(long id, @NotNull QuadFunction<String, Object, Long, Boolean, Integer> consumer) {
		this.consumer = Objects.requireNonNull(consumer);
		this.id = id;
	}

	@Override
	public long getGuildID() {
		return id;
	}

	@Override
	@Nullable
	@SuppressWarnings({ "removal", "deprecation" })
	public JSONObjectAdv getConfig() {
		return data;
	}

	@Override
	public ConcurrentHashMap<String, Object> getTempData() {
		return temp;
	}

	/**
	 * Sets the data for this instance. <blockquote><b>*** THIS SHOULD ONLY BE
	 * CALLED VIA {@link WatameBotDatabase}! ***</b></blockquote>
	 * 
	 * @param result - {@link ResultSet} to parse
	 * 
	 * @throws SQLException         if the columnLabel is not valid;if a database
	 *                              access error occurs or this method is called on
	 *                              a closed result set
	 * @throws NullPointerException thrown if there is no element passed
	 * 
	 * @see WatameBotDatabase#pushJSONUpdate(String, Object, long, boolean)
	 */
	@SuppressWarnings({ "removal", "deprecation" })
	void setData(@NotNull ResultSet result) throws SQLException {
		// Get our configuration column
		String jsonString = result.getString("GuildProperties");

		WatameBotDatabase.sqlLogger.debug(WatameBotDatabase.SQL_MARKER, "SetData <- [{}] {}", id, jsonString);

		if (jsonString == null) {
			WatameBotDatabase.logger.warn("JSON STRING IS NULL FOR " + id,
					new NullPointerException("JSON STRING IS NULL FOR " + id));
			return;
		}

		// Set our current data and pass our update method
		this.data = new JSONObjectAdv(jsonString, (key, obj, remove) -> { consumer.apply(key, obj, id, remove); });

		this.setup = true;
	}

	@Override
	public String toString() {
		return "GiuldData [setup=" + setup + ", id=" + id + ", temp=" + temp + ", config=" + data + "]";
	}
}
