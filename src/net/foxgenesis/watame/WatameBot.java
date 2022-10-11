package net.foxgenesis.watame;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.foxgenesis.watame.plugin.AWatamePlugin;
import net.foxgenesis.watame.plugin.InteractionHandler;
import net.foxgenesis.watame.plugin.PluginConstructor;
import net.foxgenesis.watame.sql.DataManager;
import net.foxgenesis.watame.sql.IDatabaseManager;

/**
 * Class containing WatameBot implementation
 * 
 * @author Ashley
 */
public class WatameBot {
	// ------------------------------- STATIC ====================
	/**
	 * General purpose bot logger
	 */
	public static Logger logger = LoggerFactory.getLogger(WatameBot.class);

	private static WatameBot instance;

	public static WatameBot getInstance() {
		return instance;
	}

	// ------------------------------- INSTNACE ====================

	/**
	 * the JDA object
	 */
	private final JDA discord;

	/**
	 * Database connection handler
	 */
	private final DataManager database;

	/**
	 * Current state of the bot
	 */
	private State state = State.CONSTRUCTING;

	/**
	 * List of all plugins
	 */
	private Collection<AWatamePlugin> plugins;

	/**
	 * Create a new instance with a specified login {@code token}.
	 * 
	 * @param token - Token used to connect to discord
	 * @throws SQLException - When failing to connect to the database file
	 */
	WatameBot(@Nonnull String token) throws SQLException {
		Objects.requireNonNull(token);

		if (instance != null)
			throw new UnsupportedOperationException("WatameBot instance already created");

		// Connect to our database file
		database = new DataManager();

		plugins = loadPlugins();

		// Create connection to discord through our token
		discord = createJDA(token);

		instance = this;
	}

	/**
	 * NEED_JAVADOC
	 */
	protected void preInit() {
		// Set our state to pre-init
		state = State.PRE_INIT;
		logger.trace("STATE = " + state);

		// Pre-initialize all plugins
		Thread pluginPreInit = new Thread(() -> plugins.parallelStream().forEach(plugin -> plugin.preInit(this)),
				"Plugin pre-init thread");
		pluginPreInit.start();

		// Setup and connect to the database
		databaseInit();

		// Wait for all plugins to be have pre-initialized
		try {
			pluginPreInit.join();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Setup and connect to the database
	 */
	private void databaseInit() {
		// Setup database with a specific resource
		logger.debug("Setting up database...");
		try {
			logger.trace("Connecting to database");
			database.connect();
		} catch (IOException e) {
			// Some error occurred while setting up database
			ExitCode.DATABASE_SETUP_ERROR.programExit(e);
		} catch (IllegalArgumentException e) {
			// Resource was null
			ExitCode.DATABASE_INVALID_SETUP_FILE.programExit(e);
		} catch (UnsupportedOperationException e) {
			// Unable to connect to database
			ExitCode.DATABASE_NOT_CONNECTED.programExit(e);
		} catch (SQLException e) {
			// Error while accessing database
			ExitCode.DATABASE_ACCESS_ERROR.programExit(e);
		}

		waitUntilReady();

		// Add all guilds that will be used to the database
		logger.trace("Adding guilds to data manager");
		discord.getGuildCache().acceptStream(stream -> stream.parallel().forEach(database::addGuild));

		// Get all database data
		logger.trace("Retrieving all data");
		database.retrieveDatabaseData(discord);
	}

	/**
	 * If JDA isn't ready, wait for it
	 */
	private void waitUntilReady() {
		if (discord.getStatus() != Status.CONNECTED)
			try {
				// Wait for JDA to be ready for use (BLOCKING!).
				logger.info("Waiting for JDA to be ready...");
				discord.awaitReady();
			} catch (InterruptedException e) {
			}
		logger.info("Connected to discord!");
	}

	/**
	 * NEED_JAVADOC
	 */
	protected void init() {
		// Set our state to init
		state = State.INIT;
		logger.trace("STATE = " + state);

		// Initialize all plugins
		plugins.parallelStream().forEach(plugin -> plugin.init(this));

		// Get global and guild interactions
		logger.trace("Getting integrations");
		SnowflakeCacheView<Guild> guildCache = discord.getGuildCache();

		Collection<CommandData> interactions = plugins.parallelStream()
				.map(plugin -> ((InteractionHandler) plugin.getInteractionHandler()).getAllInteractions(guildCache))
				.filter(cmdData -> cmdData != null).reduce((a, b) -> {
					a.addAll(b);
					return a;
				}).orElse(new ArrayList<CommandData>());

		// Tell JDA to update our command list
		logger.info("Adding {} integrations", interactions.size());
		discord.updateCommands().addCommands(interactions).queue();
	}

	/**
	 * NEED_JAVADOC
	 */
	protected void postInit() {
		// Set our state to post-init
		state = State.POST_INIT;
		logger.trace("STATE = " + state);

		// Post-initialize all plugins
		plugins.parallelStream().forEach(plugin -> plugin.postInit(this));

		// Display our game as ready
		logger.debug("Setting presence to ready");
		discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("type <help>"));

		// Set our state to running
		state = State.RUNNING;
		logger.trace("STATE = " + state);
	}

	/**
	 * Create and connect to discord with specified {@code token} via JDA.
	 * 
	 * @param token - Token used to connect to discord
	 * @return connected JDA object
	 */
	private JDA createJDA(@Nonnull String token) {
		Objects.nonNull(token);

		JDA discordTmp = null;
		boolean built = false;

		// Setup our JDA with wanted values
		logger.debug("Creating JDA");
		JDABuilder builder = JDABuilder
				.create(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES,
						GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES)
				.setMemberCachePolicy(MemberCachePolicy.ALL).setChunkingFilter(ChunkingFilter.NONE)
				.setAutoReconnect(true).setActivity(Activity.playing("Initializing..."))
				.setStatus(OnlineStatus.DO_NOT_DISTURB);

		plugins.forEach(builder::addEventListeners);

		// Attempt to connect to discord. If failed because no Internet, wait 10 seconds
		// and retry.
		do {
			try {
				// Attempt to login to discord
				logger.info("Attempting to login to discord");
				discordTmp = builder.build();

				// We connected. Stop loop.
				built = true;
			} catch (LoginException ex) {
				// Failed to connect. Log error
				logger.warn("Failed to connect: " + ex.getLocalizedMessage() + " retrying...", ex);

				// Sleep for one second before
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
			}

		} while (!built);

		if (discordTmp == null) {
			ExitCode.JDA_BUILD_FAIL.programExit("Failed to build JDA");
			return null;
		}

		return discordTmp;
	}

	/**
	 * Load and construct all plugins
	 * 
	 * @return a {@link Collection} of plugins or {@code null} on fail
	 */
	private Collection<AWatamePlugin> loadPlugins() {
		PluginConstructor constructor = new PluginConstructor();

		try {
			return constructor.loadPlugins(this);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
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
			database.close();
		} catch (Exception e) {
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
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public IDatabaseManager getDatabase() {
		return database;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public JDA getJDA() {
		return discord;
	}

	/**
	 * Get the current state of the bot.
	 * 
	 * @return Returns the {@link State} of the bot
	 * @see State
	 */
	public State getState() {
		return state;
	}

	/**
	 * States {@link WatameBot} goes through on startup.
	 * 
	 * @author Ashley
	 */
	public enum State {
		// NEED_JAVADOC javadoc needed for enum
		CONSTRUCTING, PRE_INIT, INIT, POST_INIT, RUNNING
	}
}
