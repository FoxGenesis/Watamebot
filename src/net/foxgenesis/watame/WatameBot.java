package net.foxgenesis.watame;

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
	 * Create a new instance with a specified login {@code token}.
	 * 
	 * @param token - Token used to connect to discord
	 */
	public WatameBot(String token) {
		// connect the JDA to the bot account through the token passed from the Consts
		// object
		discord = createJDA(token);
	}

	/**
	 * NEED_JAVADOC
	 */
	protected void preInit() {
		// Display our game as starting up
		logger.debug("Setting presence to initalizing");
		discord.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing("Initializing..."));
	}

	/**
	 * NEED_JAVADOC
	 */
	protected void init() {

	}

	/**
	 * NEED_JAVADOC
	 */
	protected void postInit() {
		// Display our game as ready
		logger.debug("Setting presence to ready");
		discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("type <help>"));
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

		// TODO close SQLLite connection

		// Disconnect from discord
		if (discord != null) {
			logger.debug("Shutting down JDA...");
			discord.shutdown();
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
	 * NEED_JAVADOC WatameBot.isConnectedToDatabase()
	 * 
	 * @return
	 */
	public boolean isConnectedToDatabase() {
		// IMPLEMENT get method to check if JDBC is connected to the database
		return false;
	}
}
