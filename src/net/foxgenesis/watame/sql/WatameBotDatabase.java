package net.foxgenesis.watame.sql;

import java.sql.ResultSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.watame.Constants;

public class WatameBotDatabase extends net.foxgenesis.database.AbstractDatabase implements IDatabaseManager {
	// =============================== STATIC =================================
	static final Logger logger = LoggerFactory.getLogger("Database");
	static final Logger sqlLogger = LoggerFactory.getLogger("SQLInfo");

	static final Marker SQL_MARKER = MarkerFactory.getMarker("SQL");

	static final Marker UPDATE_MARKER = MarkerFactory.getMarker("SQL_UPDATE");
	static final Marker QUERY_MARKER = MarkerFactory.getMarker("SQL_QUERY");

	static {
		UPDATE_MARKER.add(SQL_MARKER);
		QUERY_MARKER.add(SQL_MARKER);
	}

	// =============================== INSTANCE =================================

	/**
	 * Map of guild id to guild data object
	 */
	private final ConcurrentHashMap<Long, GuildData> data = new ConcurrentHashMap<>();

	public WatameBotDatabase() {
		super("WatameBot Database", Constants.DATABASE_OPERATIONS_FILE, Constants.DATABASE_SETUP_FILE);
	}

	/**
	 * Register a guild to be loaded during data retrieval.
	 * 
	 * @param guild - {@link Guild} to be loaded
	 * @throws NullPointerException if {@code guild} is {@code null}
	 * @see #removeGuild(Guild)
	 */
	public void addGuild(@Nonnull Guild guild) {
		Objects.requireNonNull(guild);

		logger.debug("Loading guild ({})[{}]", guild.getName(), guild.getIdLong());

		this.data.put(guild.getIdLong(), new GuildData(guild, this::pushJSONUpdate));

		// If initial data retrieval has already been completed, retrieve needed data
		retrieveData(guild);

		logger.trace("Guild loaded ({})[{}]", guild.getName(), guild.getIdLong());
	}

	/**
	 * Remove a guild from the data manager.
	 * 
	 * @param guild - {@link Guild} to be removed
	 * @throws NullPointerException if {@code guild} is {@code null}
	 * @see #addGuild(Guild)
	 */
	public void removeGuild(@Nonnull Guild guild) {
		Objects.requireNonNull(guild);

		logger.debug("Removing guild ({})[{}]", guild.getName(), guild.getIdLong());

		try {
			this.data.remove(guild.getIdLong()).close();
			logger.trace("Guild REMOVED ({})[{}]", guild.getName(), guild.getIdLong());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Insert a new row into the database for a {@link Guild}.
	 * 
	 * @param guild - guild to insert
	 */
	private void insertGuildInDatabase(@Nonnull Guild guild) {
		Objects.requireNonNull(guild);

		this.prepareStatement("guild_data_insert", st -> {
			long guildID = guild.getIdLong();
			st.setLong(1, guildID);

			sqlLogger.trace(UPDATE_MARKER, st.toString());
			System.out.println("update: " + st.executeUpdate());
		}, e -> sqlLogger.error(QUERY_MARKER, "Error while inserting new guild", e));
	}

	/**
	 * Retrieve guild data for a specific guild.
	 * 
	 * @param guild - {@link Guild} to retrieve data for
	 */
	private void retrieveData(@Nonnull Guild guild) {
		Objects.requireNonNull(guild);

		this.prepareStatement("guild_data_get_id", s -> {
			s.setLong(1, guild.getIdLong());
			sqlLogger.trace(QUERY_MARKER, s.toString());

			try (ResultSet set = s.executeQuery()) {
				if (set.next()) {
					long id = set.getLong("GuildID"); //$NON-NLS-1$
					if (id != 0 && this.data.containsKey(id)) {
						this.data.get(id).setData(set);
						return;
					}
				}
				logger.warn("Guild ({})[{}] is missing in database! Attempting to insert and retrieve...",
						guild.getName(), guild.getIdLong());
				insertGuildInDatabase(guild);
				retrieveData(guild);
			}
		}, e -> sqlLogger.error(QUERY_MARKER, "Error while reading guild", e));
	}

	@Override
	@Nullable
	public IGuildData getDataForGuild(@Nonnull Guild guild) {
		// Ensure non null guild
		Objects.requireNonNull(guild);

		// Check if database processing is finished
		if (!isReady())
			throw new UnsupportedOperationException("Data not ready yet");

		// Check and get guild data
		if (!data.containsKey(guild.getIdLong())) {
			// Guild data doesn't exist
			logger.warn("Attempted to get non existant data for guild {}. Attempting to insert and retrieve data",
					guild.getId());

			insertGuildInDatabase(guild);
			retrieveData(guild);
		}

		return data.get(guild.getIdLong());
	}

	/**
	 * Method used to either update or remove JSON data in the database.
	 * 
	 * @param name   - JSON name path
	 * @param data   - data to set or {@code null} if removing
	 * @param remove - should the data at {@code name} be removed or updated
	 * @return
	 * @throws NullPointerException     if {@code name} is {@code null}
	 * @throws IllegalArgumentException if {@code remove} is {@code true} and
	 *                                  {@code data} is {@code null}
	 * @see #setData(ResultSet)
	 */
	private Integer pushJSONUpdate(@Nonnull String name, @Nullable Object data, @Nonnull Guild guild, boolean remove) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(guild);

		int result;
		if (remove) {
			result = this.mapStatement("guild_json_remove", removeStatement -> {
				// Set data and execute update
				removeStatement.setString(1, "$." + name);
				removeStatement.setLong(3, guild.getIdLong());

				sqlLogger.debug(UPDATE_MARKER, "PushUpdate -> " + removeStatement);

				return removeStatement.executeUpdate();
			}, e -> logger.error(UPDATE_MARKER, "Error while removing guild json data", e));

		} else {
			// Ensure we have data passed
			if (data == null)
				throw new IllegalArgumentException("Data must not be null if 'remove' is 'false'!");

			result = this.mapStatement("guild_json_update", updateStatement -> {
				// Set data and execute update
				updateStatement.setString(1, "$." + name);
				updateStatement.setString(2, data.toString());
				updateStatement.setLong(3, guild.getIdLong());

				sqlLogger.debug(UPDATE_MARKER, "PushUpdate -> " + updateStatement);

				return updateStatement.executeUpdate();
			}, e -> logger.error(UPDATE_MARKER, "Error while updating guild json data", e));
		}
		sqlLogger.debug(UPDATE_MARKER, "ExecuteUpdate <- " + result);

		return result;
	}

	@Override
	public void close() throws Exception {}

	@Override
	protected void onReady() {}
}
