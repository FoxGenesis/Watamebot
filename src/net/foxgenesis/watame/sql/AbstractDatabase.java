package net.foxgenesis.watame.sql;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.config.KVPFile;
import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.watame.ExitCode;

/**
 * NEED_JAVADOC
 *
 * @author Ashley
 *
 */
public class AbstractDatabase implements AutoCloseable {

	/**
	 * Database logger
	 */
	@Nonnull
	protected final Logger logger;

	@Nullable
	private final URL databaseSetupFile;

	@Nonnull
	private final URL databaseOperationsFile;

	/**
	 * On close methods
	 */
	@Nonnull
	private final List<Runnable> onClose = new ArrayList<>();

	/**
	 * SQL connection pool
	 */
	@Nonnull
	protected DataSource source;

	/**
	 * Map of registered statements. This is to ensure we do not make more of the
	 * same prepared statements.
	 */
	@Nonnull
	private final ConcurrentHashMap<String, String> registeredStatements = new ConcurrentHashMap<>();

	/**
	 * NEED_JAVADOC
	 *
	 * @param properties
	 */
	public AbstractDatabase(@Nonnull DatabaseProperties properties) {
		Objects.requireNonNull(properties);

		logger = LoggerFactory.getLogger(properties.name());

		this.source = Objects.requireNonNull(properties.source());
		this.databaseSetupFile = Objects.requireNonNull(properties.setupFile());
		this.databaseOperationsFile = Objects.requireNonNull(properties.operationsFile());
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @throws SQLException
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	public synchronized void setup() throws SQLException, UnsupportedOperationException, IOException {
		if (Objects.nonNull(databaseSetupFile))
			setupDatabase(databaseSetupFile);

		if (Objects.nonNull(databaseOperationsFile))
			initalizeOperations(databaseOperationsFile);
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
		Objects.requireNonNull(url);

		// Read all lines in the file
		logger.trace("Reading lines from SQL file");
		List<String> lines = ResourceUtils.linesFromResource(url);

		try (Connection conn = source.getConnection()) {
			// Iterate on each line
			for (String line : lines) {
				if (line.startsWith("--"))
					continue;
				try (Statement statement = conn.createStatement()) {

					// Execute the current line
					logger.trace("Executing SQL statement: " + line);
					statement.execute(line);
				}
			}
		} catch (SQLException e) {
			ExitCode.DATABASE_SETUP_ERROR.programExit(e);
			return;
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
	 * NEED_JAVADOC
	 *
	 * @param id
	 * @param statement
	 * @throws SQLException
	 */
	private void registerStatement(String id, @Nonnull String statement) throws SQLException {
		Objects.requireNonNull(statement);

		// Check if id is already registered
		if (registeredStatements.containsKey(id))
			throw new IllegalArgumentException("Statement '" + id + "' already exists!");

		// Register our statement to ensure no duplicates are made
		logger.trace("Creating PreparedStatement {} : {}", id, statement);
		registeredStatements.put(id, statement);
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @param id
	 * @return
	 */
	@CheckForNull
	protected String getRawStatement(String id) { return registeredStatements.get(id); }

	/**
	 *
	 * @param id
	 * @return
	 */
	@Nullable
	protected String assertRawStatement(String id) {
		String statement = getRawStatement(id);
		if (statement != null)
			return statement;
		ExitCode.DATABASE_STATEMENT_MISSING.programExit(String.format("Statement with id '%s' is not registered!", id));
		return null;
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @param <R>
	 * @param id
	 * @param function
	 * @return
	 */
	protected <R> R mapStatement(@Nonnull String id, @Nonnull StatementFunction<R> function) {
		return mapStatement(id, function, null);
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @param <R>
	 * @param id
	 * @param function
	 * @return
	 */
	protected <R> R mapStatement(String id, @Nonnull StatementFunction<R> function,
			Consumer<SQLException> errorHandler) {
		String raw = assertRawStatement(id);

		try (Connection conn = source.getConnection(); PreparedStatement statement = conn.prepareStatement(raw)) {
			return function.apply(statement);
		} catch (SQLException e) {
			if (errorHandler != null)
				errorHandler.accept(e);
			else
				e.printStackTrace();
		}
		return null;
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @param id
	 * @param consumer
	 */
	protected void callStatement(@Nonnull String id, @Nonnull StatementConsumer consumer) {
		callStatement(id, consumer, null);
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @param id
	 * @param consumer
	 * @param errorHandler
	 */
	protected void callStatement(@Nonnull String id, @Nonnull StatementConsumer consumer,
			Consumer<SQLException> errorHandler) {
		mapStatement(id, statement -> {
			consumer.accept(statement);
			return null;
		}, errorHandler);
	}

	/**
	 * Create the database folder if it doesn't exist
	 *
	 * @param file - {@link File} used as the database folder
	 * @throws IllegalArgumentException if folder exists and is not a directory
	 * @throws NullPointerException     if {@code folder} is null
	 */
	@SuppressWarnings("unused")
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
			try {
				// Repository file doesn't exist. Make new one
				logger.info("Repo file does not exist! Creating...");
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @param onClose
	 */
	public void addCloseHandler(Runnable onClose) { this.onClose.add(onClose); }

	/**
	 * NEED_JAVADOC
	 *
	 * @param toRemove
	 */
	public void removeCloseHandler(Runnable toRemove) {
		if (this.onClose.contains(toRemove))
			this.onClose.remove(toRemove);
	}

	@Override
	public synchronized void close() throws Exception { this.onClose.forEach(Runnable::run); }

	@Override
	public String toString() {
		return "AbstractDatabase [source=" + source + ", databaseSetupFile=" + databaseSetupFile
				+ ", databaseOperationsFile=" + databaseOperationsFile + ", registeredStatements="
				+ registeredStatements + "]";
	}

	@FunctionalInterface
	protected interface StatementFunction<R> { public R apply(PreparedStatement statement) throws SQLException; }

	@FunctionalInterface
	protected interface StatementConsumer { public void accept(PreparedStatement statement) throws SQLException; }
}
