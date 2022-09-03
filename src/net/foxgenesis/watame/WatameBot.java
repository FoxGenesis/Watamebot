package net.foxgenesis.watame;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

/**
 * Class containing WatameBot implementation
 * @author Ashley
 */
public class WatameBot {
	/**
	 * General purpose bot logger
	 */
	public static Logger logger = Logger.getLogger(WatameBot.class.getName());

	/**
	 * the JDA object
	 */
	private final JDA discord;

	/**
	 * Create a new instance with a specified login {@code token}.
	 * @param token - Token used to connect to discord
	 */
	public WatameBot(String token) {
		// connect the JDA to the bot account through the token passed from the Consts
		// object
		discord = createJDA(token);
	}

	/**
	 * Create and connect to discord with specified {@code token} via JDA.
	 * @param token - Token used to connect to discord
	 * @return connected JDA object
	 */
	private JDA createJDA(String token) {
		JDA discordTmp = null;
		boolean connected = false;
		
		// Setup our JDA with wanted values
		JDABuilder builder = JDABuilder
				.createDefault(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES,
						GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)
				.setMemberCachePolicy(MemberCachePolicy.ALL);
		
		// Attempt to connect to discord. If failed because no Internet, wait 10 seconds and retry.
		do {
			try {
				// Attempt to login to discord
				discordTmp = builder.build();
				
				// We connected. Stop loop.
				connected = true;
			} catch(LoginException ex) {
				// Failed to connect. Log error
				logger.log(Level.WARNING,"Failed to connect: " + ex.getLocalizedMessage() + " retrying...", ex);
				
				// Sleep for one second before 
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
			}
			
		} while(!connected);

		// Enable auto-reconnect in case we get disconnected.
		discordTmp.setAutoReconnect(true);
		
		// Display our game as starting up
		discordTmp.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Initializing..."));
		
		try {
			// Wait for JDA to be ready for use (BLOCKING!).
			discordTmp.awaitReady();
		} catch (InterruptedException e) {}
		
		return discord;
	}

	/**
	 * Bot shutdown method.
	 * <p>
	 * This method will be called on program exit.
	 * </p>
	 */
	void shutdown() {
		logger.log(Level.FINE, "Shutting down...");
		
		//TODO close SQLLite connection
		
		// Disconnect from discord
		if(discord != null) {
			logger.log(Level.FINE, "Shutting down JDA...");
			discord.shutdown();
		}
		
		logger.log(Level.INFO, "Exiting...");
	}
	
	/**
	 * Check if this instances {@link JDA} is built and connected to Discord.
	 * @return {@link JDA} instance is built and its current status is {@link Status#CONNECTED}.
	 */
	public boolean isConnectedToDiscord() {
		return discord != null && discord.getStatus() == Status.CONNECTED;
	}
	
	/**
	 * NEED_JAVADOC WatameBot.isConnectedToDatabase()
	 * @return
	 */
	public boolean isConnectedToDatabase() {
		//IMPLEMENT get method to check if JDBC is connected to the database
		return false;
	}
}
