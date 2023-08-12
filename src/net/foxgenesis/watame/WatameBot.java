package net.foxgenesis.watame;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import net.foxgenesis.database.AConnectionProvider;
import net.foxgenesis.database.DatabaseManager;
import net.foxgenesis.database.IDatabaseManager;
import net.foxgenesis.database.providers.MySQLConnectionProvider;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.property.database.LCKConfigurationDatabase;
import net.foxgenesis.util.MethodTimer;
import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.PluginHandler;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.property.ImmutablePluginProperty;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyProvider;
import net.foxgenesis.watame.property.impl.PluginPropertyProviderImpl;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.IOUtil;

/**
 * Class containing WatameBot implementation
 *
 * @author Ashley
 */
public class WatameBot {
	// ------------------------------- STATIC ====================
	/**
	 * General purpose logger
	 */
	public static final Logger logger = LoggerFactory.getLogger(WatameBot.class);

	/**
	 * Singleton instance of class
	 */
	public static final WatameBot INSTANCE;

	/**
	 * Settings that were parsed at startup
	 */
	private static final WatameBotSettings settings;

	/**
	 * watame.ini configuration file
	 */
	private static final ImmutableConfiguration config;

	static {
		settings = Main.getSettings();
		config = settings.getConfiguration();

		// initialize the main bot object with token
		INSTANCE = new WatameBot(settings.getToken());
	}

	// ------------------------------- INSTNACE ====================

	/**
	 * Builder to bulid jda
	 */
	private JDABuilder builder;

	/**
	 * the JDA object
	 */
	private JDA discord;

	// ============================================================================

	/**
	 * Database connection handler
	 */
	private final DatabaseManager manager;

	/**
	 * Database connection provider
	 */
	private AConnectionProvider connectionProvider;

	/**
	 * Plugin configuration database
	 */
	private final LCKConfigurationDatabase propertyDatabase;

	/**
	 * Plugin configuration provider
	 */
	private PluginPropertyProvider propertyProvider;

	/**
	 * Property containing a channel to log messages to
	 */
	private PluginProperty loggingChannel;

	// ============================================================================

	/**
	 * Current state of the bot
	 */
	private State state = State.CONSTRUCTING;

	/**
	 * Plugin handler
	 */
	private final PluginHandler<@NotNull Plugin> pluginHandler;

	/**
	 * Instance context
	 */
	private final Context context;

	/**
	 * Create a new instance with a specified login {@code token}.
	 *
	 * @param token - Token used to connect to discord
	 *
	 * @throws SQLException When failing to connect to the database file
	 */
	private WatameBot(@NotNull String token) {
		// Update our state
		updateState(State.CONSTRUCTING);
		logger.debug("Creating WatameBot instance");

		// Set shutdown thread
		logger.debug("Adding shutdown hook");
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "WatameBot Shutdown Thread"));

		// Create our database manager
		manager = new DatabaseManager("WatameBot Database Manager");

		// Create database connection
		try {
			connectionProvider = new MySQLConnectionProvider(ResourceUtils
					.getProperties(Path.of("config", "database.properties"), Constants.DATABASE_SETTINGS_FILE));
		} catch (IOException e) {
			try {
				ExitCode.DATABASE_SETUP_ERROR.programExit(e);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}

		// Create our plugin property database
		propertyDatabase = new LCKConfigurationDatabase(connectionProvider.getDatabase(),
				Constants.DATABASE_TABLE_PROPERTIES, Constants.DATABASE_TABLE_PROPERTY_INFO);
		propertyProvider = new PluginPropertyProviderImpl(propertyDatabase, Constants.PLUGIN_PROPERTY_CACHE_TIME);

		// Create discord connection builder
		builder = createJDA(token, null);

		// Set our instance context
		context = new Context(this, builder, null);

		// Create our plugin handler
		pluginHandler = new PluginHandler<>(context, getClass().getModule().getLayer(), Plugin.class);
	}

	void start() throws Exception {
		logger.info("Starting WatameBot");

		long start = System.nanoTime();

		// Update our state to constructing
		updateState(State.CONSTRUCTING);

		// Set our state to pre-init
		updateState(State.PRE_INIT);
		preInit();

		// Set our state to init
		updateState(State.INIT);
		init();

		// Set our state to post-init
		updateState(State.POST_INIT);
		postInit();

		long end = System.nanoTime();

		// Set our state to running
		updateState(State.RUNNING);
		logger.info("Startup completed in {} seconds", MethodTimer.formatToSeconds(end - start));
		ready();
	}

	/**
	 * Bot shutdown method.
	 * <p>
	 * This method will be called on program exit.
	 * </p>
	 */
	private void shutdown() {
		// Set our state to shutdown
		updateState(State.SHUTDOWN);

		logger.info("Shutting down...");

		IOUtil.silentClose(pluginHandler);

		// Disconnect from discord
		if (discord != null) {
			logger.info("Shutting down JDA...");
			discord.shutdown();
		}

		// Close connection to datebase
		try {
			logger.info("Closing database connection");
			if (manager != null)
				manager.close();
		} catch (Exception e) {
			logger.error("Error while closing database connection!", e);
		}

		// Await all futures to complete
		if (!ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES))
			logger.warn("Timed out waiting for common pool shutdown. Continuing shutdown...");

		logger.info("Exiting...");
	}

	private void construct() throws Exception {
		/*
		 * ====== CONSTRUCTION ======
		 */
		// Construct plugins
		pluginHandler.loadPlugins();
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @throws Exception
	 */
	private void preInit() throws Exception {
		/*
		 * ====== PRE-INITIALIZATION ======
		 */

		// Pre-initialize all plugins async
		CompletableFuture<Void> pluginPreInit = pluginHandler.preInit();

		// Setup the database
		try {
			logger.debug("Adding database to database manager");
			Plugin integrated = pluginHandler.getPlugin("integrated");
			if (integrated == null)
				throw new SeverePluginException("Failed to find the integrated plugin!");
			manager.register(integrated, propertyDatabase);
		} catch (IOException e) {
			// Some error occurred while setting up database
			ExitCode.DATABASE_SETUP_ERROR.programExit(e);
		} catch (IllegalArgumentException e) {
			// Resource was null
			ExitCode.DATABASE_INVALID_SETUP_FILE.programExit(e);
		}

		/*
		 * ====== END PRE-INITIALIZATION ======
		 */

		// Wait for all plugins to be have pre-initialized
		logger.debug("Waiting for plugin pre-initialization");
		pluginPreInit.join();

		logger.info("Starting database pool");
		manager.start(connectionProvider, null).join();
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @throws Exception
	 */
	private void init() throws Exception {
		/*
		 * ====== INITIALIZATION ======
		 */
		// Assert that the moderation log property is set
		Plugin integrated = pluginHandler.getPlugin("integrated");
		if (integrated != null)
			loggingChannel = propertyProvider.upsertProperty(integrated, "modlog", true, PropertyType.NUMBER);

		// Initialize all plugins
		CompletableFuture<Void> pluginInit = pluginHandler.init();
		/*
		 * ====== END INITIALIZATION ======
		 */

		logger.debug("Waiting for plugin initialization");
		pluginInit.join();
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @throws Exception
	 */
	private void postInit() throws Exception {
		/*
		 * ====== POST-INITIALIZATION ======
		 */
		logger.info("Connecting to discord");
		discord = buildJDA();
		context.onJDABuilder(discord);

		// Post-initialize all plugins
		CompletableFuture<Void> pluginPostInit = pluginHandler.postInit(this);

		// Register commands
		logger.info("Collecting commands...");
		pluginHandler.updateCommands(discord.updateCommands()).queue();

		/*
		 * ====== END POST-INITIALIZATION ======
		 */

		logger.debug("Waiting for plugin post-initialization");
		pluginPostInit.join();

		// Wait for discord to be ready
		if (discord.getStatus() != Status.CONNECTED)
			try {
				// Wait for JDA to be ready for use (BLOCKING!).
				logger.info("Waiting for JDA to be ready...");
				discord.awaitReady();
			} catch (InterruptedException e) {}
		logger.info("Connected to discord!");
	}

	private void ready() {
		// Display our game as ready
		logger.debug("Setting presence to ready");
		discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity
				.playing(config.getString("WatameBot.Status.online", "https://github.com/FoxGenesis/Watamebot")));

		// Fire on ready event
		pluginHandler.onReady(this);
	}

	/**
	 * Create and connect to discord with specified {@code token} via JDA.
	 *
	 * @param token         - Token used to connect to discord
	 * @param eventExecutor - JDA event pool
	 *
	 * @return connected JDA object
	 */
	private JDABuilder createJDA(String token, ExecutorService eventExecutor) {
		Objects.requireNonNull(token, "Login token must not be null");

		// Setup our JDA with wanted values
		logger.debug("Creating JDA");
		JDABuilder builder = JDABuilder
				.create(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MODERATION,
						GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
				.disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER,
						CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
				.setChunkingFilter(ChunkingFilter.ALL).setAutoReconnect(true)
				.setActivity(Activity.playing(config.getString("WatameBot.Status.startup", "Initalizing...")))
				.setMemberCachePolicy(MemberCachePolicy.ALL).setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setEnableShutdownHook(false);

		// Set JDA's event pool executor
		if (eventExecutor != null)
			builder.setEventPool(eventExecutor, true);
		return builder;
	}

	private JDA buildJDA() throws Exception {
		JDA discordTmp = null;
		boolean built = false;

		// Attempt to connect to discord. If failed because no Internet, wait 10 seconds
		// and retry.
		do {
			try {
				// Attempt to login to discord
				logger.info("Attempting to login to discord");
				discordTmp = builder.build();

				// We connected. Stop loop.
				built = true;
			} catch (Exception ex) {
				// Failed to connect. Log error
				logger.warn("Failed to connect: [" + ex.getLocalizedMessage() + "]! Retrying in 5 seconds...", ex);

				// Sleep for five seconds before
				try {
					Thread.sleep(5_000);
				} catch (InterruptedException e) {}
			}

		} while (!built);

		if (discordTmp == null) {
			ExitCode.JDA_BUILD_FAIL.programExit("Failed to build JDA");
			return null;
		}

		return discordTmp;
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
	public IDatabaseManager getDatabaseManager() {
		return manager;
	}

	public PluginPropertyProvider getPropertyProvider() {
		return propertyProvider;
	}

	public ImmutablePluginProperty getLoggingChannel() {
		return loggingChannel;
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
	 *
	 * @see State
	 */
	public State getState() {
		return state;
	}

	private void updateState(State state) {
		this.state = state;
		logger.trace("STATE = " + state);
		System.setProperty("watame.status", state.name());
	}

	/**
	 * States {@link WatameBot} goes through on startup.
	 *
	 * @author Ashley
	 */
	public enum State {
		/**
		 * NEED_JAVADOC
		 */
		CONSTRUCTING,
		/**
		 * NEED_JAVADOC
		 */
		PRE_INIT,
		/**
		 * NEED_JAVADOC
		 */
		INIT,
		/**
		 * NEED_JAVADOC
		 */
		POST_INIT,
		/**
		 * WatameBot has finished all loading stages and is running
		 */
		RUNNING,
		/**
		 * WatameBot is shutting down
		 */
		SHUTDOWN;

		public final Marker marker;

		State() {
			marker = MarkerFactory.getMarker(name());
		}
	}
}
