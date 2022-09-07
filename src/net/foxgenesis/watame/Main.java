package net.foxgenesis.watame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.util.ProgramArguments;
import net.foxgenesis.util.SingleInstanceUtil;

/**
 * Program main class
 * @author Ashley
 */
public class Main {
	
	/**
	 * Global logger
	 */
	public static final Logger logger = LoggerFactory.getLogger(Main.class);
	/**
	 * Program entry point
	 * @param args - program arguments
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		logger.info("Starting...");
		
		if(logger.isDebugEnabled())
			logger.info("Debugging enabled");
		
		try {
			// Attempt to obtain instance lock
			logger.debug("Attempting to obtain instance lock");
			SingleInstanceUtil.waitAndGetLock(5);
		} catch(SingleInstanceUtil.SingleInstanceLockException e) {
			// Another instance is already running
			ExitCode.INSTANCE_ALREADY_RUNNING.programExit("Another instance is already running! Exiting...");
			return;
		}
		
		// Parse program arguments
		ProgramArguments params = new ProgramArguments(args);
		
		// Check if the token parameter was passed in
		if(!params.hasParameter("token"))
			ExitCode.NO_TOKEN.programExit("No token file specified");
		
		// Get discord login token from file
		String token = readToken(params.getParameter("token"));

		// initialize the main bot object with token
		logger.debug("Creating WatameBot instance");
		WatameBot watame = new WatameBot(token);
		
		// Set shutdown thread
		logger.debug("Adding shutdown hook");
		Runtime.getRuntime().addShutdownHook(new Thread(watame::shutdown, "WatameBot Shutdown Thread"));
		
		// Load needed resources for initialization
		logger.debug("Pre-Initialization...");
		watame.preInit();
		
		// Initialization
		logger.info("Initializing...");
		watame.init();
		
		// Post initialization
		logger.debug("Post-Initialization...");
		watame.postInit();
		
		logger.info("Startup Complete!");
	}

	/**
	 * NEED_JAVADOC
	 * @return
	 */
	private static String readToken(String filepath) {
		logger.debug("Getting token from file");
		
		// Read token from file
		try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
			// obtain and return the token
			return br.readLine();
		} catch (IOException ex) {
			ExitCode.INVALID_TOKEN.programExit(ex);
		}
		// Failed to read the token
		return null;
	}
}
