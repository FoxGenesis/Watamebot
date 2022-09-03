package net.foxgenesis.watame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import net.foxgenesis.util.ProgramArguments;

/**
 * Program main class
 * @author Ashley
 */
public class Main {
	
	/**
	 * Global logger
	 */
	private static final Logger logger = Logger.getGlobal();

	/**
	 * Program entry point
	 * @param args - program arguments
	 */
	public static void main(String[] args) {
		// Parse program arguments
		ProgramArguments params = new ProgramArguments(args);
		
		// Check if the token parameter was passed in
		if(!params.hasParameter("token"))
			ExitCode.NO_TOKEN.programExit("No token file specified");
		
		// Get discord login token from file
		String token = readToken(params.getParameter("token"));

		// initialize the main bot object with token
		logger.finer("Creating WatameBot instance");
		WatameBot watame = new WatameBot(token);
		
		// Set shutdown thread
		logger.finer("Adding shutdown hook");
		Runtime.getRuntime().addShutdownHook(new Thread(watame::shutdown, "WatameBot Shutdown Thread"));
	}

	/**
	 * NEED_JAVADOC
	 * @return
	 */
	private static String readToken(String filepath) {
		logger.finer("Getting token from file");
		
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
