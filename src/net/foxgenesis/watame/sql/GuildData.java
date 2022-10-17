package net.foxgenesis.watame.sql;

import java.sql.PreparedStatement;
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
	private final DataManager dataManager;

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
	GuildData(@Nonnull Guild guild, @Nonnull DataManager dataManager) {
		Objects.nonNull(dataManager);
		this.dataManager = dataManager;

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
	void setData(@Nonnull ResultSet result) throws SQLException {
		Objects.nonNull(result);

		// Check if we have something to parse
//		if (result.isBeforeFirst())
//			if (!result.next())
//				throw new NullPointerException("NO CONFIG IN RESULT SET!");

		// Get our configuration column
		String jsonString = result.getString("GuildProperties");
		
		DataManager.sqlLogger.debug(DataManager.SQL_MARKER, "SetData <- [{}] {}", guild.getName(), jsonString);

		if (jsonString == null) {
			DataManager.logger.warn("JSON STRING IS NULL FOR " + guild.getIdLong(),
					new NullPointerException("JSON STRING IS NULL FOR " + guild.getIdLong()));
			return;
		}

		// Set our current data and pass our update method
		this.data = new JSONObjectAdv(jsonString, this::pushJSONUpdate);
		
		this.setup = true;
	}

	/**
	 * Method used to either update or remove JSON data in the database.
	 * 
	 * @param name   - JSON name path
	 * @param data   - data to set or {@code null} if removing
	 * @param remove - should the data at {@code name} be removed or updated
	 * @throws NullPointerException     if {@code name} is {@code null}
	 * @throws IllegalArgumentException if {@code remove} is {@code true} and
	 *                                  {@code data} is {@code null}
	 * @see #setData(ResultSet)
	 */
	private void pushJSONUpdate(@Nonnull String name, @Nullable Object data, boolean remove) {
		Objects.nonNull(name);

		int result;

		if (remove)
			try (PreparedStatement removeStatement = dataManager.getAndAssertStatement("guild_json_remove")) {
				// Set data and execute update
				removeStatement.setString(1, "$." + name);

				DataManager.sqlLogger.debug(DataManager.UPDATE_MARKER, "PushUpdate -> " + removeStatement);

				result = removeStatement.executeUpdate();
			} catch (SQLException e) {
				DataManager.logger.error(DataManager.UPDATE_MARKER, "Error while removing guild json data", e);
				return;
			}
		else {
			// Ensure we have data passed
			if (data == null)
				throw new IllegalArgumentException("Data must not be null if 'remove' is 'true'!");

			try (PreparedStatement updateStatement = dataManager.getAndAssertStatement("guild_json_update")) {
				// Set data and execute update
				updateStatement.setString(1, "$." + name);
				updateStatement.setString(2, data.toString());
				updateStatement.setLong(3, guild.getIdLong());

				DataManager.sqlLogger.debug(DataManager.UPDATE_MARKER, "PushUpdate -> " + updateStatement);

				result = updateStatement.executeUpdate();
			} catch (SQLException e) {
				DataManager.logger.error(DataManager.UPDATE_MARKER, "Error while updating guild json data", e);
				return;
			}
		}

		DataManager.sqlLogger.debug(DataManager.UPDATE_MARKER, "ExecuteUpdate <- " + result);
	}
	
	private void checkSetup() {
		if(!setup)
			throw new UnsupportedOperationException("GuildData has not been setup yet!");
	}

	@Override
	public void close() throws Exception {
		temp.clear();
	}
	
	@Override
	public String toString() {
		return "GiuldData [setup=" + setup + ", guild=" + guild + ", temp=" + temp +", config=" + data + "]";
	}
}
