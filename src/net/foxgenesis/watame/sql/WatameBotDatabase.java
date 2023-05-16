package net.foxgenesis.watame.sql;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.database.AbstractDatabase;
import net.foxgenesis.watame.Constants;
import net.foxgenesis.watame.property.GuildProperty;
import net.foxgenesis.watame.property.IGuildPropertyProvider;

public class WatameBotDatabase extends AbstractDatabase implements IGuildDataProvider, IGuildPropertyProvider {
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

	private final HashMap<String, GuildProperty> properties = new HashMap<>();

	public WatameBotDatabase() {
		super("WatameBot Database", Constants.DATABASE_OPERATIONS_FILE, Constants.DATABASE_SETUP_FILE);
	}

	/**
	 * Register a guild to be loaded during data retrieval.
	 * 
	 * @param guild - {@link Guild} to be loaded
	 * 
	 * @throws NullPointerException if {@code guild} is {@code null}
	 * 
	 * @see #removeGuild(Guild)
	 * 
	 * @deprecated
	 */
	@Deprecated(forRemoval = true)
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
	 * 
	 * @throws NullPointerException if {@code guild} is {@code null}
	 * 
	 * @see #addGuild(Guild)
	 * 
	 * @deprecated
	 */
	@Deprecated(forRemoval = true)
	public void removeGuild(@Nonnull Guild guild) {
		Objects.requireNonNull(guild);

		logger.debug("Removing guild ({})[{}]", guild.getName(), guild.getIdLong());

		try {
			this.data.remove(guild.getIdLong());
			logger.trace("Guild REMOVED ({})[{}]", guild.getName(), guild.getIdLong());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Register a guild to be loaded during data retrieval.
	 * 
	 * <blockquote><b>NOTE:</b> This contains a
	 * {@link ForkJoinPool.ManagedBlocker}!</blockquote>
	 * 
	 * @param id - guild ID to be removed
	 * 
	 * @throws IllegalArgumentException if {@code id} is 0
	 * 
	 * @see #removeGuild(long)
	 */
	public void addGuild(long id) {
		if (id == 0)
			throw new IllegalArgumentException("Invalid Guild ID");

		logger.debug("Loading guild [{}]", id);
		data.computeIfAbsent(id, ID -> new GuildData(id, WatameBotDatabase.this::pushJSONUpdate));

		try {
			ForkJoinPool.managedBlock(new ManagedBlocker() {

				@Override
				public boolean block() throws InterruptedException {
					if (data.containsKey(id) && !data.get(id).setup)
						return retrieveData(id, 5);
					return false;
				}

				@Override
				public boolean isReleasable() {
					return data.containsKey(id) && data.get(id).setup;
				}

			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove a guild from the data manager.
	 * 
	 * @param id - guild ID to be removed
	 * 
	 * @throws IllegalArgumentException if {@code id} is 0
	 * 
	 * @see #addGuild(long)
	 */
	public void removeGuild(long id) {
		if (id == 0)
			throw new IllegalArgumentException("Invalid Guild ID");

		logger.debug("Removing guild [{}]", id);
		this.data.remove(id);
	}

	/**
	 * Retrieve guild data for a specific guild.
	 * 
	 * @param guildID    - guild ID to retrieve data for
	 * @param maxRetries - max amount of retries
	 * 
	 * @return Returns {@code true} if data was retrieved
	 */
	private boolean retrieveData(long guildID, int maxRetries) {
		return retrieveData(guildID, Math.max(0, maxRetries), 0);
	}

	/**
	 * Retrieve guild data for a specific guild.
	 * 
	 * @param guildID    - guild ID to retrieve data for
	 * @param maxRetries - max amount of retries
	 * @param retry      - current retry
	 * 
	 * @return Returns {@code true} if data was retrieved
	 */
	private boolean retrieveData(long guildID, int maxRetry, int retry) {
		return retry > maxRetry ? false : guildID == 0 ? false : this.mapStatement("guild_data_get_id", s -> {
			s.setLong(1, guildID);
			sqlLogger.trace(QUERY_MARKER, s.toString());

			try (ResultSet set = s.executeQuery()) {
				if (set.next()) {
					long id = set.getLong("GuildID");
					if (id != 0 && this.data.containsKey(id)) {
						this.data.get(id).setData(set);
						return true;
					}
				}
				logger.warn("Guild [{}] is missing in database! Attempting to insert and retrieve...", guildID);
				return insertGuildInDatabase(guildID) && retrieveData(guildID, maxRetry, retry + 1);
			}
		}, e -> sqlLogger.error(QUERY_MARKER, "Error while reading guild", e));
	}

	/**
	 * Insert a new row into the database for a {@link Guild}.
	 * 
	 * @param guild - guild id to insert
	 * 
	 * @return Returns {@code true} if a row was inserted into the database
	 */
	private boolean insertGuildInDatabase(long guild) {
		return guild == 0 ? false : this.mapStatement("guild_data_insert", st -> {
			st.setLong(1, guild);

			sqlLogger.trace(UPDATE_MARKER, st.toString());

			int result = st.executeUpdate();
			sqlLogger.debug("Insert Result: ", result);
			return result > 0;
		}, e -> sqlLogger.error(QUERY_MARKER, "Error while inserting new guild", e));
	}

	/**
	 * Insert a new row into the database for a {@link Guild}.
	 * 
	 * @param guild - guild to insert
	 * 
	 * @deprecated
	 */
	@Deprecated(forRemoval = true)
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
	 * 
	 * @deprecated
	 */
	@Deprecated(forRemoval = true)
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
		else if (!data.containsKey(guild.getIdLong()))
			throw new NullPointerException("Data has not been retrieved for guild!");

		return data.get(guild.getIdLong());
	}

	/**
	 * Method used to either update or remove JSON data in the database.
	 * 
	 * @param name   - JSON name path
	 * @param data   - data to set or {@code null} if removing
	 * @param guild  - guild id
	 * @param remove - should the data at {@code name} be removed or updated
	 * 
	 * @return Returns the amount of rows changed by this update
	 * 
	 * @throws NullPointerException     if {@code name} is {@code null}
	 * @throws IllegalArgumentException if {@code remove} is {@code true} and
	 *                                  {@code data} is {@code null} <b>or</b>
	 *                                  {@code guild} is 0
	 * 
	 * @see GuildData#setData(ResultSet)
	 */
	private Integer pushJSONUpdate(@Nonnull String name, @Nullable Object data, long guild, boolean remove) {
		Objects.requireNonNull(name);
		if (guild == 0)
			throw new IllegalArgumentException("Invalid guild ID");

		int result;
		if (remove) {
			result = this.mapStatement("guild_json_remove", removeStatement -> {
				// Set data and execute update
				removeStatement.setString(1, "$." + name);
				removeStatement.setLong(2, guild);

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
				updateStatement.setLong(3, guild);

				sqlLogger.debug(UPDATE_MARKER, "PushUpdate -> " + updateStatement);

				return updateStatement.executeUpdate();
			}, e -> logger.error(UPDATE_MARKER, "Error while updating guild json data", e));
		}
		sqlLogger.debug(UPDATE_MARKER, "ExecuteUpdate <- " + result);

		return result;
	}

	@Override
	public GuildProperty getProperty(@Nonnull String key) {
		return properties.computeIfAbsent(key, k -> new GuildProperty(k, this));
	}

	@Override
	public boolean isPropertyPresent(@Nonnull String key) {
		return properties.containsKey(key);
	}

	@Override
	public void close() {
		data.clear();
		properties.clear();
	}

	@Override
	protected void onReady() {}

	@Override
	@Nonnull
	public Set<String> keySet() {
		return Set.copyOf(properties.keySet());
	}
}
