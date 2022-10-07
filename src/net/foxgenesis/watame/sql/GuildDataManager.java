package net.foxgenesis.watame.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.sql.rowset.serial.SerialBlob;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.config.fields.StorageKey;
import net.foxgenesis.util.MethodTimer;

public class GuildDataManager implements IDatabaseManager {
	private static final String ensureField = "ALTER TABLE `GuildData` ADD `%1$s` %2$s NOT NULL;"; //$NON-NLS-1$
	private static final String GET_GUILD_DATA_ID = "SELECT * FROM GuildData where `GuildID` = ?"; //$NON-NLS-1$
	private static final String GET_GUILD_DATA = "SELECT * FROM GuildData"; //$NON-NLS-1$
	private static final String CREATE_ROW = "INSERT INTO `GuildData` (`GuildID`, `Config`) VALUES (?, ?)"; //$NON-NLS-1$
	private static final String WRITE_OBJECT_SQL = "INSERT INTO `Objects`(name, object) VALUES (?, ?)"; //$NON-NLS-1$
	private static final String READ_OBJECT_SQL = "SELECT `object` FROM `Objects` WHERE id = ? LIMIT 1"; //$NON-NLS-1$
	private static final String UPDATE_OBJECT_SQL = "UPDATE `Objects` SET `object` = ? WHERE `id` = ?"; //$NON-NLS-1$
	private static final String CHECK_OBJECT_SQL = "SELECT * from `Objects` where `name` = ? LIMIT 1"; //$NON-NLS-1$

	private PreparedStatement WRITE_OBJ_STATEMENT, READ_OBJ_STATEMENT, CHECK_OBJ_STATEMENT, UPDATE_OBJ_STATEMENT,
			CREATE_ROW_STATEMENT, GET_GUILD_DATA_STATEMENT, GET_GUILD_DATA_ID_STATEMENT;
	private final PreparedStatement[] STATEMENTS = { this.WRITE_OBJ_STATEMENT, this.READ_OBJ_STATEMENT,
			this.CHECK_OBJ_STATEMENT, this.UPDATE_OBJ_STATEMENT, this.CREATE_ROW_STATEMENT,
			this.GET_GUILD_DATA_STATEMENT, this.GET_GUILD_DATA_ID_STATEMENT };

	private final ConcurrentHashMap<Long, GuildData> data = new ConcurrentHashMap<>();
	private final ArrayList<StorageKey> l = new ArrayList<>();
	private boolean ready = false;
	Connection sql;

	private static final Logger LOGGER = LoggerFactory.getLogger("Database"); //$NON-NLS-1$
	private static final Logger SQLLOGGER = LoggerFactory.getLogger("SQLInfo"); //$NON-NLS-1$

	private static final Marker SQL_MARKER = MarkerFactory.getMarker("SQL"); //$NON-NLS-1$
	static final Marker UPDATE_MARKER = MarkerFactory.getMarker("SQL_UPDATE"); //$NON-NLS-1$
	static final Marker QUERY_MARKER = MarkerFactory.getMarker("SQL_QUERY"); //$NON-NLS-1$

	static {
		UPDATE_MARKER.add(SQL_MARKER);
		QUERY_MARKER.add(SQL_MARKER);
	}

	public GuildDataManager(Connection sql) throws SQLException {
		resetConnection(sql);
	}

	public synchronized void resetConnection(Connection sql) throws SQLException {
		if (this.sql != null) {
			LOGGER.debug("Closing existing SQL connetion.."); //$NON-NLS-1$
			for (PreparedStatement a : this.STATEMENTS)
				a.close();
			this.sql.close();
		}

		this.sql = sql;
		LOGGER.debug(SQL_MARKER, "Creating prepared statements"); //$NON-NLS-1$
		this.UPDATE_OBJ_STATEMENT = sql.prepareStatement(UPDATE_OBJECT_SQL);
		this.CHECK_OBJ_STATEMENT = sql.prepareStatement(CHECK_OBJECT_SQL);
		this.WRITE_OBJ_STATEMENT = sql.prepareStatement(WRITE_OBJECT_SQL);
		this.READ_OBJ_STATEMENT = sql.prepareStatement(READ_OBJECT_SQL);
		this.CREATE_ROW_STATEMENT = sql.prepareStatement(CREATE_ROW);
		this.GET_GUILD_DATA_ID_STATEMENT = sql.prepareStatement(GET_GUILD_DATA_ID);
		this.GET_GUILD_DATA_STATEMENT = sql.prepareStatement(GET_GUILD_DATA);
	}

	public boolean addGuild(Guild guild) {
		LOGGER.debug("Loading guild (%s)[%d]", guild.getName(), guild.getIdLong()); //$NON-NLS-1$
		this.data.put(guild.getIdLong(), new GuildData(guild, this));
		if (this.ready)
			getData(guild);
		LOGGER.trace("Guild loaded (%s)[%d]", guild.getName(), guild.getIdLong()); //$NON-NLS-1$
		return true;
	}

	public boolean removeGuild(Guild guild) {
		LOGGER.debug("REMOVING guild (%s)[%l]", guild.getName(), guild.getIdLong()); //$NON-NLS-1$
		try {
			GuildData d = this.data.remove(guild.getIdLong());
			d.delete();
			LOGGER.debug("Guild REMOVED (%s)[%l]", guild.getName(), guild.getIdLong()); //$NON-NLS-1$
			return true;
		} catch (Exception e) {
			LOGGER.error("Failed to remove guild", e); //$NON-NLS-1$
			return false;
		}
	}

	public synchronized void getData() {
		List<Guild> g = Bot.getClient().getGuilds();

		LOGGER.debug("Posted rows in " + MethodTimer.runFormatMS(() -> { //$NON-NLS-1$
			try (Statement s = this.sql.createStatement()) {
				this.l.forEach(a -> {
					LOGGER.debug(SQL_MARKER, "Adding batch for storage: %s", s); //$NON-NLS-1$
					try {
						s.addBatch(String.format(GuildDataManager.ensureField, a.name, a.type));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				});
				LOGGER.debug(SQL_MARKER, "Executing storage batch..."); //$NON-NLS-1$
				s.executeBatch();
			} catch (SQLException e) {
				/* Its okay for this to fail */}
		}));

		if (Bot.DEBUG)
			g.forEach(this::createRow);

		LOGGER.info("Retrieved guild data in " + MethodTimer.runFormatMS(() -> { //$NON-NLS-1$
			PreparedStatement s = this.GET_GUILD_DATA_STATEMENT;
			SQLLOGGER.debug(QUERY_MARKER, GuildDataManager.GET_GUILD_DATA);
			try (ResultSet set = s.executeQuery()) {
				set.beforeFirst();
				while (set.next()) {
					long id = set.getLong("GuildID"); //$NON-NLS-1$
					if (this.data.containsKey(id)) {
						g.removeIf(u -> u.getLongID() == id);
						this.data.get(id).setData(set, this.l);
					}
				}
			} catch (Exception e) {
			}
		}));
		this.ready = true;
		Bot.getClient().getDispatcher().dispatch(new GuildDataReadyEvent(this));
	}

	private void getData(Guild guild) {
		createRow(guild);
		PreparedStatement s = this.GET_GUILD_DATA_ID_STATEMENT;
		SQLLOGGER.debug(QUERY_MARKER, GuildDataManager.GET_GUILD_DATA_ID.replaceAll("\\?", "%s"), guild.getStringID()); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			s.setString(0, guild.getId());
			try (ResultSet set = s.executeQuery()) {
				set.first();
				long id = set.getLong("GuildID"); //$NON-NLS-1$
				if (this.data.containsKey(id))
					this.data.get(id).setData(set, this.l);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createRow(Guild guild) {
		LOGGER.debug("Creating row for guild: " + guild.getIdLong()); //$NON-NLS-1$
		PreparedStatement st = this.CREATE_ROW_STATEMENT;
		try {
			String p1 = guild.getId(), p2 = new JSONObject().toString();
			st.setString(0, p1);
			st.setString(1, p2);
			SQLLOGGER.debug(UPDATE_MARKER, GuildDataManager.CREATE_ROW.replaceAll("\\?", "%s"), p1, p2); //$NON-NLS-1$ //$NON-NLS-2$
			st.executeUpdate();
		} catch (SQLException e) {
		}
	}

	public boolean pushStorage(StorageKey s) {
		if (this.ready)
			throw new UnsupportedOperationException(
					"StorageKeys have already been sent to the table. Unable to add another StorageKey."); //$NON-NLS-1$
		if (this.l.contains(s))
			return false;
		this.l.add(s);
		LOGGER.debug("Pushing storage: %s", s); //$NON-NLS-1$
		return true;
	}

	@Override
	public GuildData getDataForGuild(Guild guild) {
		if (!this.ready) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			if (!this.ready)
				throw new UnsupportedOperationException("Data not ready yet!"); //$NON-NLS-1$
		}
		if (guild == null)
			return null;
		if (!this.data.containsKey(guild.getIdLong()))
			createRow(guild);
		return this.data.get(guild.getIdLong());
	}

	@Override
	public boolean isReady() {
		return this.ready;
	}

	private <T extends Serializable> long writeJavaObject(String name, T object) {
		try {
			PreparedStatement pstmt = this.WRITE_OBJ_STATEMENT;

			// set input parameters
			pstmt.setString(1, name);
			pstmt.setObject(2, object);
			SQLLOGGER.debug(UPDATE_MARKER, GuildDataManager.WRITE_OBJECT_SQL.replaceAll("\\?", "%s"), name, object); //$NON-NLS-1$ //$NON-NLS-2$
			pstmt.executeUpdate();

			// get the generated key for the id
			int id = -1;
			try (ResultSet rs = pstmt.getGeneratedKeys()) {
				if (rs.next())
					id = rs.getInt(1);
			}
			pstmt.close();
			LOGGER.debug("writeJavaObject: done serializing: " + name); //$NON-NLS-1$
			return id;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private void updateJavaObject(long id, Serializable object) {
		try {
			String className = object.getClass().getName();
			PreparedStatement pstmt = this.UPDATE_OBJ_STATEMENT;
			pstmt.setString(2, id + ""); //$NON-NLS-1$
			pstmt.setBlob(1, new SerialBlob(ObjSave.serialize(object)));
			SQLLOGGER.debug(UPDATE_MARKER, String.format(GuildDataManager.UPDATE_OBJECT_SQL.replaceAll("\\?", "%s"), //$NON-NLS-1$ //$NON-NLS-2$
					String.format("(%s)%s", object.getClass(), object), id)); //$NON-NLS-1$
			pstmt.executeUpdate();
			pstmt.getGeneratedKeys().close();
			LOGGER.debug("updateJavaObject: done serializing: " + className); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private <T extends Serializable> T readJavaObject(long id) {
		try {
			PreparedStatement pstmt = this.READ_OBJ_STATEMENT;
			pstmt.setLong(1, id);
			SQLLOGGER.debug(UPDATE_MARKER, GuildDataManager.READ_OBJECT_SQL.replaceAll("\\?", "%s"), id); //$NON-NLS-1$ //$NON-NLS-2$
			try (ResultSet rs = pstmt.executeQuery()) {
				rs.next();
				Object object = ObjSave.deserialize(new SerialBlob(rs.getBlob("object")).getBinaryStream()); //$NON-NLS-1$
				String className = object.getClass().getName();
				LOGGER.debug("readJavaObject: done de-serializing: " + className); //$NON-NLS-1$
				return (T) object;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public <T extends Serializable> ObjectKey<T> getObject(String name, T def) {
		ObjectKey<T> key = getIfExists(name);
		if (key != null)
			return key;

		LOGGER.warn("Object does not exist. creating new one"); //$NON-NLS-1$

		long id = writeJavaObject(name, def);

		if (id == -1)
			throw new NullPointerException("Unable to write new object to objects table"); //$NON-NLS-1$

		return new ObjectKey<>(id, name, def, (i, n, o) -> updateJavaObject(i, o), this::readJavaObject);
	}

	@SuppressWarnings("unchecked")
	private <T extends Serializable> ObjectKey<T> getIfExists(String name) {
		PreparedStatement pstmt = this.CHECK_OBJ_STATEMENT;
		try {
			pstmt.setString(1, name);
			pstmt.executeQuery();
			try (ResultSet set = pstmt.getResultSet()) {
				if (!set.first())
					return null;
				return new ObjectKey<>(set.getInt("id"), name, //$NON-NLS-1$
						(T) ObjSave.deserialize(new SerialBlob(set.getBlob("object")).getBinaryStream()), //$NON-NLS-1$
						(i, n, o) -> updateJavaObject(i, o), this::readJavaObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
