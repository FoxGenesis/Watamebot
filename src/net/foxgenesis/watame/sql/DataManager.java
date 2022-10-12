package net.foxgenesis.watame.sql;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.foxgenesis.config.KVPFile;
import net.foxgenesis.util.MethodTimer;
import net.foxgenesis.util.ResourceHelper;
import net.foxgenesis.watame.Constants;
import net.foxgenesis.watame.ExitCode;

/**
 * Class to connect and retrieve data from a database
 * 
 * @author Ashley
 *
 */
public class DataManager implements IDatabaseManager, AutoCloseable {
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

	/**
	 * Database folder used to store database files. Specifically the database file
	 */
	private final File databaseFolder;

	/**
	 * Conditional for when all guild data has been received and processed
	 */
	private boolean allDataReady;

	/**
	 * SQL connection handler
	 */
	private Connection connectionHandler;

	/**
	 * Map of registered statements. This is to ensure we do not make more of the
	 * same prepared statements.
	 */
	private final ConcurrentHashMap<String, PreparedStatement> registeredStatements = new ConcurrentHashMap<>();

	/**
	 * Create a new instance using the default database folder of "./{@code repo}/".
	 * <p>
	 * This method is effectively equivalent to: <blockquote>
	 * 
	 * <pre>
	 * new DataManager(new File("repo"))
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @throws IllegalArgumentException if folder exists and is not a directory
	 * @see #DataManager(File)
	 */
	public DataManager() {
		this(new File("repo"));
	}

	/**
	 * Create a new instance using the specified folder as the repository folder.
	 * 
	 * @param folder - {@link File} that will be used as the repository folder
	 * @throws IllegalArgumentException if {@code folder} exists and is not a
	 *                                  directory
	 * @throws NullPointerException     if {@code folder} is null
	 * @see #DataManager()
	 */
	public DataManager(@Nonnull File folder) {
		// Ensure repository folder is created
		createDatabaseFolder(folder);

		this.databaseFolder = folder;
	}

	/**
	 * Attempt to connect to the database.
	 * 
	 * @throws SQLException                  if a database access error occurs
	 * @throws IOException                   thrown if an error occurs while reading
	 *                                       the InputStream of the resource
	 * @throws UnsupportedOperationException if connection to the database failed
	 */
	public void connect() throws SQLException, UnsupportedOperationException, IOException {
		// Build connection path
		String connectionPath = databaseFolder.getPath() + File.separator + "database.db";
		logger.trace("Database path: {}", connectionPath);

		// Attempt to connect to database file
		logger.trace("Attempting connection to database");
		connectionHandler = DriverManager.getConnection("jdbc:sqlite:" + connectionPath);

		logger.info("Connected to SQL database");

		setupDatabase(Constants.DATABASE_SETUP_FILE);
		initalizeOperations(Constants.DATABASE_OPERATIONS_FILE);
	}

	/**
	 * Register a guild to be loaded during data retrieval.
	 * 
	 * @param guild - {@link Guild} to be loaded
	 * @throws NullPointerException if {@code guild} is {@code null}
	 * @see #removeGuild(Guild)
	 */
	public void addGuild(@Nonnull Guild guild) {
		Objects.nonNull(guild);

		logger.debug("Loading guild ({})[{}]", guild.getName(), guild.getIdLong());

		this.data.put(guild.getIdLong(), new GuildData(guild, this));

		// If initial data retrieval has already been completed, retrieve needed data
		if (this.allDataReady)
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
		Objects.nonNull(guild);

		logger.debug("Removing guild ({})[{}]", guild.getName(), guild.getIdLong());

		try {
			this.data.remove(guild.getIdLong()).close();
			logger.trace("Guild REMOVED ({})[{}]", guild.getName(), guild.getIdLong());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve all data needed from the database.
	 * <p>
	 * This will fire a {@link DatabaseLoadedEvent} on the passed {@code jda}
	 * instance on completion.
	 * </p>
	 * 
	 * @param jda - {@link JDA} instance to use
	 */
	public void retrieveDatabaseData(@Nonnull JDA jda) {
		Objects.nonNull(jda);

		// Get all guilds from database
		getAllGuildData();

		// Insert all missing guilds to database
//		logger.debug("Inserting missing guilds into database");
//		jda.getGuildCache()
//				.acceptStream(stream -> stream.filter(guild -> !data.contains(guild.getIdLong()))
//						.peek(guild -> logger.debug("Inserting {} into database", guild.getName()))
//						.forEach(this::insertGuildInDatabase));

		// All data has been retrieved
		this.allDataReady = true;
	}

	/**
	 * Get all the guild data for our added guilds
	 * 
	 * @see #addGuild(Guild)
	 */
	private void getAllGuildData() {
		logger.trace("Getting all guild data...");
		logger.info("Retrieved initial guild data in " + MethodTimer.runFormatMS(() -> {
			// Get prepared statement to get all guild data
			PreparedStatement s = this.getAndAssertStatement("guild_data_get");
			sqlLogger.trace(QUERY_MARKER, s.toString());

			// Execute statement
			try (ResultSet set = s.executeQuery()) {
				// set.beforeFirst();

				// Iterate over retrieved data
				while (set.next()) {
					long id = set.getLong("GuildID");

					// Check if we are to load the data and pass the result set
					if (this.data.containsKey(id)) {
						logger.debug("Adding data for guild {}", id);
						this.data.get(id).setData(set);
					}
				}

			} catch (SQLException e) {
				sqlLogger.error(QUERY_MARKER, "Error while getting initial guild data", e);
			}
		}));
	}

	/**
	 * Retrieve guild data for a specific guild.
	 * 
	 * @param guild - {@link Guild} to retrieve data for
	 */
	private void retrieveData(@Nonnull Guild guild) {
		Objects.nonNull(guild);

		logger.trace("Retrieved guild data for {} in: " + MethodTimer.runFormatMS(() -> {
			PreparedStatement s = this.getAndAssertStatement("guild_data_get_id");

			try {
				s.setString(1, guild.getId());
				sqlLogger.trace(QUERY_MARKER, s.toString());

				try (ResultSet set = s.executeQuery()) {
					// set.first();
					long id = set.getLong("GuildID"); //$NON-NLS-1$
					if (this.data.containsKey(id))
						this.data.get(id).setData(set);
				}
			} catch (SQLException e) {
				sqlLogger.error(QUERY_MARKER, "Error while getting guild data", e);
			}
		}), guild.getName());
	}

	@Override
	@Nullable
	public IGuildData getDataForGuild(@Nonnull Guild guild) {
		// Ensure non null guild
		Objects.nonNull(guild);

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
	 * Insert a new row into the database for a {@link Guild}.
	 * 
	 * @param guild - guild to insert
	 */
	private void insertGuildInDatabase(@Nonnull Guild guild) {
		Objects.nonNull(guild);

		logger.trace("Inserted new row for guild {} in: " + MethodTimer.runFormatMS(() -> {
			PreparedStatement st = this.getAndAssertStatement("guild_data_insert");
			try {
				long guildID = guild.getIdLong();
				st.setLong(1, guildID);

				sqlLogger.trace(UPDATE_MARKER, st.toString());
				st.executeUpdate();
			} catch (SQLException e) {
				sqlLogger.error(QUERY_MARKER, "Error while inserting new guild", e);
			}
		}), guild.getName());
	}

	@Override
	public boolean isReady() {
		return this.allDataReady;
	}

	@Override
	public boolean isConnectionValid() {
		try {
			// Check if our connection is valid with a one second timeout
			return connectionHandler != null && connectionHandler.isValid(1000);
		} catch (SQLException e) {
			logger.error("Error while checking database connection", e);
		}
		return false;
	}

	/**
	 * Create the database folder if it doesn't exist
	 * 
	 * @param folder - {@link File} used as the database folder
	 * @throws IllegalArgumentException if folder exists and is not a directory
	 * @throws NullPointerException     if {@code folder} is null
	 */
	private void createDatabaseFolder(@Nonnull File folder) {
		Objects.nonNull(folder);

		// Check if folder exists
		if (folder.exists()) {
			// Ensure file is a directory
			if (folder.isFile())
				throw new IllegalArgumentException("Selected file is not a directory!");
		} else {
			// Repository folder doesn't exist. Make new one
			logger.info("Repo folder does not exist! Creating...");
			folder.mkdirs();
		}
	}

	/**
	 * Setup database with SQL code from a {@link URL}.
	 * 
	 * @param url - {@link URL} containing SQL code to run
	 * @throws IOException                   Thrown if IO error occurs during file
	 *                                       processing
	 * @throws UnsupportedOperationException Thrown if not connected to database
	 */
	private void setupDatabase(@Nonnull URL url) throws IOException, UnsupportedOperationException {
		Objects.nonNull(url);

		// Check if we are connected to database
		if (!isConnectionValid())
			throw new UnsupportedOperationException("Not connected to database!");

		// Read all lines in the file
		logger.trace("Reading lines from SQL file");
		List<String> lines = ResourceHelper.linesFromResource(url);

		// Iterate on each line
		for (String line : lines) {
			try (Statement statement = connectionHandler.createStatement()) {

				// Execute the current line
				sqlLogger.trace("Executing SQL statement: " + line);
				statement.execute(line);
			} catch (SQLException e) {
				ExitCode.DATABASE_SETUP_ERROR.programExit(e);
				return;
			}
		}
	}

	/**
	 * Load all database operations from a KVP (Key. Value. Pair) resource file and
	 * map the values to {@link PreparedStatement PreparedStatements}.
	 * 
	 * @param url - {@link URL} path to a KVP resource
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 * @see KVPFile#KVPFile(URL)
	 */
	private void initalizeOperations(@Nonnull URL url) throws IOException {
		Objects.nonNull(url);

		// Read and parse database operations
		KVPFile kvp = new KVPFile(url);
		kvp.parse();

		// Map all database operations to their statements
		kvp.forEach((key, value) -> {
			if (registeredStatements.containsKey(key)) {
				logger.error("Statement '{}' already exists! Skipping...", key);
			} else {
				try {
					registerStatement(key, value);
				} catch (SQLException e) {
					ExitCode.DATABASE_STATEMENT_ERROR.programExit(e);
					return;
				}
			}
		});
	}

	/**
	 * Compile a new {@link PreparedStatement} linked with {@code id} in the
	 * database.
	 * 
	 * @param id        - statement id
	 * @param statement - SQL code to prepare
	 * @return Compiled {@link PreparedStatement} that is ready to be used
	 * @throws UnsupportedOperationException or database connection is not valid
	 * @throws IllegalArgumentException      Thrown if id is already registered
	 * @throws SQLException                  if a database access error occurs or
	 *                                       this method is called on a closed
	 *                                       connection
	 * @see #hasStatement(String)
	 * @see #isValid()
	 */
	private void registerStatement(String id, @Nonnull String statement) throws SQLException {
		Objects.nonNull(statement);

		// Check if id is already registered
		if (registeredStatements.containsKey(id))
			throw new IllegalArgumentException("Statement '" + id + "' already exists!");

		// Check if connection is valid and ready
		if (!isConnectionValid())
			throw new UnsupportedOperationException("Not connected to database!");

		// Create our statement
		sqlLogger.trace("Creating PreparedStatement {} : {}", id, statement);
		PreparedStatement preStatement = connectionHandler.prepareStatement(statement);

		// Register our statement to ensure no duplicates are made
		registeredStatements.put(id, preStatement);
	}

	/**
	 * Get a {@link PreparedStatement} registered with key {@code id} and ensure it
	 * exists. If the statement does not exist, this method will cause a program
	 * exit with exit code {@link ExitCode.DATABASE_STATEMENT_MISSING}.
	 * 
	 * @param id - key of the registered statement
	 * @return the {@link PreparedStatement} if it is registered
	 */
	PreparedStatement getAndAssertStatement(@Nonnull String id) {
		Objects.nonNull(id);

		if (registeredStatements.containsKey(id))
			return registeredStatements.get(id);

		ExitCode.DATABASE_STATEMENT_MISSING.programExit(String.format("Statement with id '%s' is not registered!", id));
		return null;
	}

	@Override
	public void close() throws Exception {
		// Check if we are connected
		if (connectionHandler != null && !connectionHandler.isClosed()) {
			// Close open statements
			closeStatements();

			// Close our connection
			logger.info("Disconnecting from database");
			connectionHandler.close();
		} else {
			logger.warn("Attempted to close already closed sql connection");
		}
	}

	/**
	 * Close all open statements
	 */
	private void closeStatements() {
		// Close all statements
		logger.trace("Closing open statements");
		registeredStatements.entrySet().stream().forEach(e -> {
			try {
				// Close the statement
				logger.trace("Closing statement " + e.getKey());
				e.getValue().close();
			} catch (SQLException e1) {
				// Failed for some reason
				logger.error("Failed to close statement '" + e.getKey() + "'!", e1);
			}
		});

		// All open statements have been closed
		logger.trace("All statements have been closed");

		// Clear our map
		registeredStatements.clear();
	}

	public static class DatabaseLoadedEvent extends Event {
		public final IDatabaseManager dataManager;

		public DatabaseLoadedEvent(JDA api, IDatabaseManager dataManager) {
			super(api);
			this.dataManager = dataManager;
		}
	}
}
