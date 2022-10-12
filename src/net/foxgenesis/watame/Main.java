package net.foxgenesis.watame;

import java.sql.SQLException;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.util.ProgramArguments;
import net.foxgenesis.util.SingleInstanceUtil;

/**
 * Program main class
 * 
 * @author Ashley
 */
public class Main {

	/**
	 * Global logger
	 */
	public static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * Program arguments
	 */
	private static ProgramArguments params;

	/**
	 * Program entry point
	 * 
	 * @param args - program arguments
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		if (logger.isDebugEnabled())
			logger.info("Debugging enabled");

		// Parse program arguments
		params = new ProgramArguments(args);

		if (!params.hasFlag("dev")) {
			logger.trace("Installing ansi console");
			AnsiConsole.systemInstall();
		}

		System.out.println();
		logger.info("Starting...");

		try {
			// Attempt to obtain instance lock
			logger.debug("Attempting to obtain instance lock");
			SingleInstanceUtil.waitAndGetLock(5);
		} catch (SingleInstanceUtil.SingleInstanceLockException e) {
			// Another instance is already running
			ExitCode.INSTANCE_ALREADY_RUNNING.programExit("Another instance is already running! Exiting...");
			return;
		}

		// First call of WatameBot class. Will cause instance creation
		WatameBot watame = WatameBot.getInstance();

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
	 * Get the {@link ProgramArguments} of this application.
	 * 
	 * @return flags, arguments and parameters used to launch this application
	 */
	static ProgramArguments getProgramArguments() {
		return params;
	}
}
