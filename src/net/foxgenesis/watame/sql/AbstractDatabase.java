package net.foxgenesis.watame.sql;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.config.KVPFile;
import net.foxgenesis.util.ResourceHelper;
import net.foxgenesis.watame.ExitCode;

/**
 * Abstract class used to create SQLite databases. Class is able to create
 * database file, setup tables and create {@link PreparedStatement
 * PreparedStatements} for all operations.
 *
 * @author Ashley
 *
 */
public class AbstractDatabase implements AutoCloseable {

	/**
	 * Database logger
	 */
	protected final Logger logger;

	/**
	 * Database folder used to store database files. Specifically the database file
	 */
	@Nonnull
	private final File databaseFile;

	@Nonnull
	private final URL databaseSetupFile;

	@Nonnull
	private final URL databaseOperationsFile;

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
	 * new RoleStorageDatabase(new File("repo"))
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @throws IllegalArgumentException if folder exists and is not a directory
	 * @see #AbstractDatabase(File, URL, URL, String)
	 */
	public AbstractDatabase(@Nonnull URL databaseSetupFile, @Nonnull URL databaseOperationsFile) {
		this(new File("repo/database.db"), databaseSetupFile, databaseOperationsFile, "Database");
	}

	/**
	 * Create and initialize a new database at the specified file. Additionally,
	 * creating prepared operations for use.
	 *
	 * @param databaseFile           - {@link File} that will be used as the
	 *                               repository
	 * @param databaseSetupFile      - {@link URL} pointing to a file containing SQL
	 *                               operations to initialize the new database
	 * @param databaseOperationsFile - {@link URL} pointing to a KVP (Key Value
	 *                               Pair) file containing SQL statements to create
	 *                               {@link PreparedStatement PreparedStatements}
	 *                               for
	 * @param name                   - name to use for logging
	 * @throws IllegalArgumentException if {@code folder} exists and is not a
	 *                                  directory
	 * @throws NullPointerException     if {@code folder} is null
	 * @see #AbstractDatabase(URL, URL)
	 */
	public AbstractDatabase(@Nonnull File databaseFile, @Nonnull URL databaseSetupFile,
			@Nonnull URL databaseOperationsFile, @Nonnull String name) {

		logger = LoggerFactory.getLogger("Role Storage");

		this.databaseFile = Objects.requireNonNull(databaseFile);
		this.databaseSetupFile = Objects.requireNonNull(databaseSetupFile);
		this.databaseOperationsFile = Objects.requireNonNull(databaseOperationsFile);
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
		createDatabaseFile(databaseFile);

		// Build connection path
		String connectionPath = databaseFile.getPath();
		logger.trace("Database path: {}", connectionPath);

		// Attempt to connect to database file
		logger.trace("Attempting connection to database");
		connectionHandler = DriverManager.getConnection("jdbc:sqlite:" + connectionPath);

		logger.info("Connected to SQL database");

		setupDatabase(databaseSetupFile);
		initalizeOperations(databaseOperationsFile);
	}

	/**
	 * Check if the database connection is valid. This method checks if our
	 * connection is not {@code null} and then asks the {@link Connection} if it's
	 * valid with a one second timeout.
	 *
	 * @return If the database connection is valid and usable
	 */
	public boolean isConnectionValid() {
		try {
			// Check if our connection is valid with a one second timeout
			return Objects.nonNull(connectionHandler) && connectionHandler.isValid(1000);
		} catch (SQLException e) {
			logger.error("Error while checking database connection", e);
		}
		return false;
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
	 * @see #getAndAssertStatement(String)
	 */
	private void registerStatement(String id, @Nonnull String statement) throws SQLException {
		Objects.requireNonNull(statement);

		// Check if id is already registered
		if (registeredStatements.containsKey(id))
			throw new IllegalArgumentException("Statement '" + id + "' already exists!");

		// Check if connection is valid and ready
		if (!isConnectionValid())
			throw new UnsupportedOperationException("Not connected to database!");

		// Create our statement
		logger.trace("Creating PreparedStatement {} : {}", id, statement);
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
	protected PreparedStatement getAndAssertStatement(@Nonnull String id) {
		Objects.requireNonNull(id);

		if (registeredStatements.containsKey(id))
			return registeredStatements.get(id);

		ExitCode.DATABASE_STATEMENT_MISSING.programExit(String.format("Statement with id '%s' is not registered!", id));
		return null;
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
		// Check if we are connected to database
		if (!isConnectionValid())
			throw new UnsupportedOperationException("Not connected to database!");

		// Read all lines in the file
		logger.trace("Reading lines from SQL file");
		List<String> lines = ResourceHelper.linesFromResource(url);

		// Iterate on each line
		for (String line : lines) {
			if (line.startsWith("--"))
				continue;
			try (Statement statement = connectionHandler.createStatement()) {

				// Execute the current line
				logger.trace("Executing SQL statement: " + line);
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
	 * Create the database folder if it doesn't exist
	 *
	 * @param file - {@link File} used as the database folder
	 * @throws IllegalArgumentException if folder exists and is not a directory
	 * @throws NullPointerException     if {@code folder} is null
	 */
	private void createDatabaseFile(@Nonnull File file) {
		Objects.requireNonNull(file);

		// Ensure folder is created
		File folder = file.getParentFile();
		if (!folder.exists())
			folder.mkdirs();

		// Check if file exists
		if (file.exists()) {
			// Ensure file is a file
			if (!file.isFile())
				throw new IllegalArgumentException("Selected file is a directory!");
		} else {
			// Repository file doesn't exist. Make new one
			logger.info("Repo file does not exist! Creating...");
			file.mkdirs();
		}
	}

	@Override
	public void close() throws SQLException {
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

	@Override
	public String toString() {
		return "AbstractDatabase [databaseFile=" + databaseFile + ", databaseSetupFile=" + databaseSetupFile
				+ ", databaseOperationsFile=" + databaseOperationsFile + ", connectionHandler=" + connectionHandler
				+ ", registeredStatements=" + registeredStatements + "]";
	}
}
