package net.foxgenesis.watame.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handles the SQL connection and registers
 * prepared statements
 * @author Ashley
 *
 */
public class WatameDatabase implements DatabaseHandler {
	/**
	 *  logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(WatameDatabase.class);

	/**
	 * Map of registered statements. This is to ensure we do not
	 * make more of the same prepared statements.
	 */
	private final HashMap<String, PreparedStatement> registeredStatements = new HashMap<>();
	
	/**
	 * SQL connection handler
	 */
	private final Connection connectionHandler;
	
	
	/**
	 * Create a new instance of the database 
	 * @throws SQLException
	 */
	public WatameDatabase() throws SQLException {
		
		File f = new File("repo");
		if (!f.exists()) {
			logger.info("Repo folder does not exist! Creating...");
			f.mkdirs();
		}
		
		// Attempt to connect to database file
		logger.debug("Attempting connection to database");
		connectionHandler = DriverManager.getConnection("jdbc:sqlite:repo" + File.separatorChar + "database.db");
		
		logger.info("Connected to SQL database");
	}

	@Override
	public PreparedStatement registerStatement(String id, String statement) throws SQLException {
		// Check if id is already registered
		if(registeredStatements.containsKey(id))
			throw new UnsupportedOperationException("Statement '" + id + "' already exists!");
		
		// Check if connection is valid and ready
		if(!isValid())
			throw new UnsupportedOperationException("Not connected to database!");
		
		// Create our statement
		logger.debug("Creating PreparedStatement " + id + ": " + statement);
		PreparedStatement preStatement = connectionHandler.prepareStatement(statement);
		
		// Register our statement to ensure no duplicates are made
		registeredStatements.put(id, preStatement);
		
		// Return out statement
		return preStatement;
	}

	@Override
	public boolean isValid() {
		try {
			// Check if our connection is valid with a one second timeout
			return connectionHandler.isValid(1000);
		} catch (SQLException e) {
			logger.error("Error while checking database connection", e);
		}
		return false;
	}
	
	@Override
	public boolean hasStatement(String id) {
		// Check if the statement has already been made
		return registeredStatements.containsKey(id);
	}
	
	/**
	 * Close all open statements and database connection
	 * @throws SQLException
	 */
	public void shutdown() throws SQLException {
		// Check if we are connected
		if(connectionHandler != null && !connectionHandler.isClosed()) {
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
		logger.debug("Closing open statements");
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
		logger.debug("All statements have been closed");
		
		// Clear our map
		registeredStatements.clear();
	}
}
