package net.foxgenesis.watame;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.foxgenesis.watame.functionality.ABotFunctionality;
import net.foxgenesis.watame.sql.DatabaseHandler;
import net.foxgenesis.watame.sql.WatameDatabase;

/**
 * Class containing WatameBot implementation
 * 
 * @author Ashley
 */
public class WatameBot {
	/**
	 * General purpose bot logger
	 */
	public static Logger logger = LoggerFactory.getLogger(WatameBot.class);

	/**
	 * the JDA object
	 */
	private final JDA discord;
	
	/**
	 * Database connection handler
	 */
	private final WatameDatabase database;
	
	/**
	 * Current state of the bot
	 */
	private State state = State.CONSTRUCTING;

	/**
	 * Create a new instance with a specified login {@code token}.
	 * 
	 * @param token - Token used to connect to discord
	 * @throws SQLException - When failing to connect to the
	 * database file
	 */
	public WatameBot(String token) throws SQLException {
		// Create connection to discord through our token
		discord = createJDA(token);
		
		// Connect to our database file
		database = new WatameDatabase();
	}

	/**
	 * NEED_JAVADOC
	 */
	protected void preInit() {
		// Set our state to pre-init
		state = State.PRE_INIT;
		
		// Display our game as starting up
		logger.debug("Setting presence to initalizing");
		discord.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("Initializing..."));
		
		// Setup database with a specific resource
		logger.debug("Setting up database...");
		try {
			database.setupDatabase(Constants.DATABASE_SETUP_FILE);
		} catch (IOException e) {
			// Some error occurred while setting up database
			ExitCode.DATABASE_SETUP_ERROR.programExit(e);
		} catch(IllegalArgumentException e) {
			// Resource was null
			ExitCode.DATABASE_INVALID_SETUP_FILE.programExit(e);
		}
	}

	/**
	 * NEED_JAVADOC
	 */
	protected void init() {
		// Set our state to init
		state = State.INIT;
	}

	/**
	 * NEED_JAVADOC
	 */
	protected void postInit() {
		// Set our state to post-init
		state = State.POST_INIT;
		
		// Display our game as ready
		logger.debug("Setting presence to ready");
		discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("type <help>"));
		
		// Set our state to running
		state = State.RUNNING;
	}

	/**
	 * Create and connect to discord with specified {@code token} via JDA.
	 * 
	 * @param token - Token used to connect to discord
	 * @return connected JDA object
	 */
	private JDA createJDA(String token) {
		JDA discordTmp = null;
		boolean connected = false;

		// Setup our JDA with wanted values
		logger.debug("Creating JDA");
		JDABuilder builder = JDABuilder
				.createDefault(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES,
						GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)
				.setMemberCachePolicy(MemberCachePolicy.ALL);

		// Attempt to connect to discord. If failed because no Internet, wait 10 seconds
		// and retry.
		do {
			try {
				// Attempt to login to discord
				logger.info("Attempting to login to discord");
				discordTmp = builder.build();

				// We connected. Stop loop.
				connected = true;
			} catch (LoginException ex) {
				// Failed to connect. Log error
				logger.warn("Failed to connect: " + ex.getLocalizedMessage() + " retrying...", ex);

				// Sleep for one second before
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
			}

		} while (!connected);

		logger.info("Connected to discord!");

		// Enable auto-reconnect in case we get disconnected.
		discordTmp.setAutoReconnect(true);

		try {
			// Wait for JDA to be ready for use (BLOCKING!).
			logger.info("Waiting for JDA to be ready...");
			discordTmp.awaitReady();
		} catch (InterruptedException e) {
		}

		return discordTmp;
	}

	/**
	 * Bot shutdown method.
	 * <p>
	 * This method will be called on program exit.
	 * </p>
	 */
	void shutdown() {
		logger.debug("Shutting down...");

		// Disconnect from discord
		if (discord != null) {
			logger.debug("Shutting down JDA...");
			discord.shutdown();
		}
		
		try {
			logger.debug("Closing database connection");
			database.shutdown();
		} catch (SQLException e) {
			logger.error("Error while closing database connection!", e);
		}

		logger.info("Exiting...");
	}

	/**
	 * Check if this instances {@link JDA} is built and connected to Discord.
	 * 
	 * @return {@link JDA} instance is built and its current status is
	 *         {@link Status#CONNECTED}.
	 */
	public boolean isConnectedToDiscord() {
		return discord != null && discord.getStatus() == Status.CONNECTED;
	}

	/**
	 * Checks if the connection to the database is valid
	 * @return Returns {@code true} if connected to the
	 * database
	 * @see WatameDatabase#isValid()
	 */
	public boolean isConnectedToDatabase() {
		return database.isValid();
	}
	
	/**
	 * NEED_JAVADOC
	 * @return
	 */
	public DatabaseHandler getDatabase() {
		return database;
	}
	
	/**
	 * NEED_JAVADOC
	 * @return
	 */
	public JDA getJDA() {
		return discord;
	}
	
	/**
	 * Get the current state of the bot.
	 * @return Returns the {@link State} of the bot
	 * @see State
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * States {@link WatameBot} goes through
	 * on startup.
	 * @author Ashley
	 */
	public enum State {
		//NEED_JAVADOC javadoc needed for enum
		CONSTRUCTING,
		PRE_INIT,
		INIT,
		POST_INIT,
		RUNNING
	}
}
