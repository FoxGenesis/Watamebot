package net.foxgenesis.watame.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.config.fields.JSONObjectAdv;
import net.foxgenesis.util.function.QuadFunction;

/**
 * Class used to contain guild database data.
 * 
 * @author Ashley
 *
 */
public class GuildData implements IGuildData, AutoCloseable {

	/**
	 * Link to parent data manager
	 */
	@Nonnull
	private final QuadFunction<String, Object, Guild, Boolean, Integer> consumer;

	/**
	 * {@link Guild} this instance is based on
	 */
	@Nonnull
	private final Guild guild;

	/**
	 * Temporary storage
	 */
	private final ConcurrentHashMap<String, Object> temp = new ConcurrentHashMap<>();

	/**
	 * JSON object used to store guild settings
	 */
	@SuppressWarnings({"removal", "deprecation" })
	private JSONObjectAdv data;

	private boolean setup = false;

	/**
	 * Creates a new instance of {@link GuildData} with a provided guild and the
	 * {@link DataManager} that created it. <blockquote><b> *** THIS SHOULD ONLY BE
	 * CALLED VIA {@link DataManager}! *** </blockquote></b>
	 * 
	 * @param guild       - The {@link Guild} that this instance represents
	 * @param dataManager - the {@link DataManager} that created this instance
	 */
	GuildData(@Nonnull Guild guild, @Nonnull QuadFunction<String, Object, Guild, Boolean, Integer> consumer) {
		Objects.nonNull(consumer);
		this.consumer = consumer;

		Objects.nonNull(guild);
		this.guild = guild;
	}

	@Override
	public Guild getGuild() {
		checkSetup();
		return guild;
	}

	@Override
	@Nullable
	@SuppressWarnings({"removal", "deprecation" })
	public JSONObjectAdv getConfig() {
		checkSetup();
		return data;
	}

	@Override
	@CheckForNull
	public Channel getLogChannel() {
		checkSetup();
		return guild.getTextChannelById(data.optLong("guild.log_channel"));
	}

	@Override
	public ConcurrentHashMap<String, Object> getTempData() {
		checkSetup();
		return temp;
	}

	/**
	 * Sets the data for this instance. <blockquote><b>*** THIS SHOULD ONLY BE
	 * CALLED VIA {@link DataManager}! ***</b></blockquote>
	 * 
	 * @param result - {@link ResultSet} to parse
	 * @throws SQLException         if the columnLabel is not valid;if a database
	 *                              access error occurs or this method is called on
	 *                              a closed result set
	 * @throws NullPointerException thrown if there is no element passed
	 * @see #pushJSONUpdate(String, Object, boolean)
	 */
	@SuppressWarnings({"removal", "deprecation" })
	void setData(@Nonnull ResultSet result) throws SQLException {
		Objects.requireNonNull(result);

		// Check if we have something to parse
		// if (result.isBeforeFirst())
		// if (!result.next())
		// throw new NullPointerException("NO CONFIG IN RESULT SET!");

		// Get our configuration column
		String jsonString = result.getString("GuildProperties");

		WatameBotDatabase.sqlLogger.debug(WatameBotDatabase.SQL_MARKER, "SetData <- [{}] {}", guild.getName(), jsonString);

		if (jsonString == null) {
			WatameBotDatabase.logger.warn("JSON STRING IS NULL FOR " + guild.getIdLong(),
					new NullPointerException("JSON STRING IS NULL FOR " + guild.getIdLong()));
			return;
		}

		// Set our current data and pass our update method
		this.data = new JSONObjectAdv(jsonString, (key, obj, remove) -> {
			consumer.apply(key, obj, guild, remove);
		});

		this.setup = true;
	}

	private void checkSetup() {
		if (!setup)
			throw new UnsupportedOperationException("GuildData has not been setup yet!");
	}

	@Override
	public void close() throws Exception { temp.clear(); }

	@Override
	public String toString() {
		return "GiuldData [setup=" + setup + ", guild=" + guild + ", temp=" + temp + ", config=" + data + "]";
	}
}
