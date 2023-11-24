package net.foxgenesis.watame;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
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
import net.foxgenesis.util.PushBullet;
import net.foxgenesis.util.resource.ResourceUtils;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.PluginHandler;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.property.ImmutablePluginProperty;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyProvider;
import net.foxgenesis.watame.property.impl.PluginPropertyProviderImpl;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
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
	private static final Logger logger = LoggerFactory.getLogger(WatameBot.class);

	/**
	 * Path pointing to the configuration directory
	 */
	public static final Path CONFIG_PATH;

	/**
	 * Utility class to retrieve information from the stack
	 */
	private static final StackWalker walker = StackWalker.getInstance(RETAIN_CLASS_REFERENCE);

	/**
	 * Push notifications helper
	 */
	private static final PushBullet pushbullet;

	/**
	 * Settings that were parsed at startup
	 */
	private static final Settings settings;

	/**
	 * watame.ini configuration file
	 */
	private static final ImmutableConfiguration config;

	/**
	 * Singleton instance of class
	 */
	static final WatameBot INSTANCE;

	static {
		settings = Main.getSettings();
		config = settings.getConfiguration();

		pushbullet = settings.getPushbullet();
		RestAction.setDefaultFailure(
				err -> pushbullet.pushPBMessage("An Error Occurred in Watame", ExceptionUtils.getStackTrace(err)));

		// Initialize our configuration path
		CONFIG_PATH = settings.getConfigPath();

		// Initialize the main bot object with token
		INSTANCE = new WatameBot(settings.getToken());
	}

	/**
	 * Get the {@link Plugin} of the calling method.
	 * <p>
	 * This method uses a {@link StackWalker} to retrieve the calling class. As
	 * such, it will only retrieve the loaded {@link Plugin} of the caller's
	 * {@link Module}.
	 * </p>
	 * 
	 * @param <U>         Wanted plugin type
	 * @param pluginClass - class of the desired {@link Plugin}
	 * 
	 * @return Returns the found {@link Plugin}
	 * 
	 * @throws NoSuchElementException Thrown if there is no plugin loaded for the
	 *                                calling module
	 * @throws ClassCastException     Thrown if the found plugin is not assignable
	 *                                to the specified {@code pluginClass}
	 * 
	 * @see #getSelfPlugin()
	 */
	public static <U extends Plugin> U getSelfPlugin(Class<U> pluginClass) {
		Class<?> callerClass = walker.getCallerClass();
		Module module = callerClass.getModule();
		Plugin p = INSTANCE.pluginHandler.getPluginForModule(module);

		if (p == null)
			throw new NoSuchElementException("No plugin is loaded for module: " + module.getName());

		return pluginClass.cast(p);
	}

	/**
	 * Get the {@link Plugin} of the calling method.
	 * <p>
	 * This method uses a {@link StackWalker} to retrieve the calling class. As
	 * such, it will only retrieve the loaded {@link Plugin} of the caller's
	 * {@link Module}.
	 * </p>
	 * 
	 * @return Returns the found {@link Plugin}
	 * 
	 * @see #getSelfPlugin(Class)
	 */
	public static Plugin getSelfPlugin() {
		Class<?> callerClass = walker.getCallerClass();
		Module module = callerClass.getModule();
		return INSTANCE.pluginHandler.getPluginForModule(module);
	}

	/**
	 * Get the plugin specified by the {@code identifier}.
	 * 
	 * @param identifier - plugin identifier
	 * 
	 * @return Returns the {@link Plugin} with the specified {@code identifier}
	 */
	public static Plugin getPlugin(String identifier) {
		return INSTANCE.pluginHandler.getPlugin(identifier);
	}

	/**
	 * Get a plugin by class.
	 * 
	 * @param pluginClass - plugin class
	 * 
	 * @return Returns the found {@link Plugin} if found, otherwise {@code null}
	 */
	public static Plugin getPluginByClass(Class<? extends Plugin> pluginClass) {
		return INSTANCE.pluginHandler.getPlugin(pluginClass);
	}

	/**
	 * Register event listeners to the calling {@link Plugin}.
	 * 
	 * @param listeners - event listeners to register
	 * 
	 * @see #removeEventListeners(Object...)
	 */
	public static void addEventListeners(Object... listeners) {
		Class<?> callerClass = walker.getCallerClass();
		Module module = callerClass.getModule();
		Plugin plugin = INSTANCE.pluginHandler.getPluginForModule(module);

		INSTANCE.context.getEventRegister().registerListeners(plugin, listeners);
	}

	/**
	 * Unregister event listeners from the calling {@link Plugin}.
	 * 
	 * @param listeners - event listeners to unregister
	 * 
	 * @see #addEventListeners(Object...)
	 */
	public static void removeEventListeners(Object... listeners) {
		Class<?> callerClass = walker.getCallerClass();
		Module module = callerClass.getModule();
		Plugin plugin = INSTANCE.pluginHandler.getPluginForModule(module);

		INSTANCE.context.getEventRegister().unregisterListeners(plugin, listeners);
	}

	/**
	 * Get the database manager used to register custom databases.
	 *
	 * @return Returns the {@link IDatabaseManager} used to register custom
	 *         databases
	 */
	public static IDatabaseManager getDatabaseManager() {
		return INSTANCE.manager;
	}

	/**
	 * Get the {@link PluginPropertyProvider} used to register/retrieve
	 * {@link PluginProperty PluginProperties}.
	 * 
	 * @return Returns the {@link PluginPropertyProvider}
	 */
	public static PluginPropertyProvider getPropertyProvider() {
		return INSTANCE.propertyProvider;
	}

	/**
	 * Get the <i>modlog</i> (Moderation Logging) channel property.
	 * 
	 * @return Returns the an {@link ImmutablePluginProperty} used to retrieve the
	 *         set <i>modlog</i> for a {@link net.dv8tion.jda.api.entities.Guild
	 *         Guild}
	 */
	public static ImmutablePluginProperty getLoggingChannel() {
		return INSTANCE.loggingChannel;
	}

	/**
	 * Get the current state of the bot.
	 *
	 * @return Returns the {@link State} of the bot
	 *
	 * @see State
	 */
	public static State getState() {
		return INSTANCE.state;
	}

	/**
	 * Get the {@link JDA} instance.
	 *
	 * @return the current instance of {@link JDA}
	 */
	public static JDA getJDA() {
		return INSTANCE.discord;
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
	 */
	private WatameBot(String token) {
		// Set shutdown thread
		logger.debug("Adding shutdown hook");
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "WatameBot Shutdown Thread"));

		// Create our database manager
		manager = new DatabaseManager("Database Manager");

		// Create database connection
		try {
			connectionProvider = getConnectionProvider();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// Create our plugin property database
		propertyDatabase = new LCKConfigurationDatabase(connectionProvider.getDatabase(),
				Constants.DATABASE_TABLE_PROPERTIES, Constants.DATABASE_TABLE_PROPERTY_INFO);
		propertyProvider = new PluginPropertyProviderImpl(propertyDatabase, Constants.PLUGIN_PROPERTY_CACHE_TIME);

		// Create discord connection builder
		builder = createJDA(token, null);

		// Set our instance context
		context = new Context(builder, null, pushbullet::pushPBMessage);

		// Create our plugin handler
		pluginHandler = new PluginHandler<>(context, getClass().getModule().getLayer(), Plugin.class);
	}

	void start() throws Exception {
		try {
			long start = System.nanoTime();

			// Update our state to constructing
			updateState(State.CONSTRUCTING);
			logger.info("Starting");
			construct();

			// Set our state to pre-init
			updateState(State.PRE_INIT);
			logger.info("Calling pre-initialization");
			preInit();

			// Set our state to init
			updateState(State.INIT);
			logger.info("Calling initialization");
			init();

			// Set our state to post-init
			updateState(State.POST_INIT);
			logger.info("Calling post-initialization");
			postInit();

			long end = System.nanoTime();

			// Set our state to running
			updateState(State.RUNNING);
			logger.info("Startup completed in {} seconds", MethodTimer.formatToSeconds(end - start));
			logger.info("Calling on ready");
			ready();
		} catch (Exception e) {
			pushbullet.pushPBMessage("An Error Occurred in Watame", ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}

	private void construct() throws Exception {
		/*
		 * ====== CONSTRUCTION ======
		 */
		// Construct plugins
		pluginHandler.loadPlugins();

		// Setup the database
		try {
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
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @throws Exception
	 */
	private void preInit() throws Exception {
		// Pre-initialize all plugins
		pluginHandler.preInit();

		logger.info("Starting database pool");
		manager.start(connectionProvider);
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @throws Exception
	 */
	private void init() throws Exception {
		// Assert that the moderation log property is set
		Plugin integrated = pluginHandler.getPlugin("integrated");
		if (integrated != null)
			loggingChannel = propertyProvider.upsertProperty(integrated, "modlog", true, PropertyType.NUMBER);

		// Initialize all plugins
		pluginHandler.init();
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

		// Post-initialize all plugins
		pluginHandler.postInit();

		discord = buildJDA();
		context.onJDABuilder(discord);

		// Register commands
		logger.info("Collecting commands...");
		pluginHandler.updateCommands(discord.updateCommands()).queue();

		/*
		 * ====== END POST-INITIALIZATION ======
		 */

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
		// Fire on ready event
		pluginHandler.onReady();

		// Display our game as ready
		logger.debug("Setting presence to ready");
		discord.getPresence().setPresence(OnlineStatus.ONLINE,
				Activity.playing(config.getString("Startup.Status.online", "https://github.com/FoxGenesis/Watamebot")));
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

		System.out.println();
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
						GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
				.disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.CLIENT_STATUS,
						CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
				.setChunkingFilter(ChunkingFilter.ALL).setAutoReconnect(true)
				.setActivity(Activity.playing(config.getString("Startup.Status.startup", "Initalizing...")))
				.setMemberCachePolicy(MemberCachePolicy.ALL).setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setEnableShutdownHook(false);

		// Set JDA's event pool executor
		if (eventExecutor != null)
			builder.setEventPool(eventExecutor, true);
		return builder;
	}

	/**
	 * Finish building JDA and attempt connection to Discord.
	 * 
	 * @return Returns the created {@link JDA} or {@code null} if connection failed
	 * 
	 * @throws Exception
	 */
	private JDA buildJDA() throws Exception {
		// Finalize required intents and caches
		logger.info("Collecting gateway intents");
		builder.enableIntents(pluginHandler.getGatewayIntents());

		logger.info("Setting up caches");
		builder.enableCache(pluginHandler.getCaches());

		logger.info("Getting all required cache policies");
		builder.setMemberCachePolicy(pluginHandler.getRequiredCachePolicy());

		// Attempt to connect to discord
		int maxTries = 5;
		JDA discordTmp = attemptConnection(() -> {
			try {
				return builder.build();
			} catch (InvalidTokenException e) {
				ExitCode.INVALID_TOKEN.programExit(e.getMessage());
				return null;
			}
		}, 2000, maxTries, "Discord");

		if (discordTmp == null) {
			ExitCode.JDA_BUILD_FAIL.programExit("Failed to build JDA after " + maxTries + " tries");
			return null;
		}

		return discordTmp;
	}

	private AConnectionProvider getConnectionProvider() throws Exception {
		Properties properties = ResourceUtils.getProperties(settings.getConfigPath().resolve("database.properties"),
				Constants.DATABASE_SETTINGS_FILE);

		int maxTries = 5;
		AConnectionProvider provider = attemptConnection(() -> new MySQLConnectionProvider(properties), 2000, maxTries,
				"database");
		if (provider == null)
			ExitCode.DATABASE_SETUP_ERROR.programExit("Failed to connect to the database after " + maxTries + " tries");
		return provider;
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
	 * Update the state of the application.
	 * 
	 * @param state - new state
	 */
	private void updateState(State state) {
		this.state = state;
		logger.trace("STATE = " + state);
		System.setProperty("watame.status", state.name());
	}

	/**
	 * Attempt to establish a connection. This method will try to connect
	 * {@code maxTries} times with a delay of <i>d * 2<sup>x</sup></i> between
	 * failures where {@code d} is {@code delay} and {@code x} is the try count.
	 * 
	 * @param <T>      connection result type
	 * @param supplier - connection supplier
	 * @param delay    - initial delay that will increase exponentially
	 * @param maxTries - max amount of attempts to connect
	 * @param msg      - string to add to logging (i.e. "the database" or "web
	 *                 server")
	 * 
	 * @return Returns the created connection or {@code null} if connection was
	 *         unable to be established after {@code maxTries} attempts
	 */
	private static <T> T attemptConnection(ConnectionSupplier<T> supplier, long delay, int maxTries, String msg) {
		T out = null;
		int tries = 0;

		while (++tries < maxTries) {
			if (tries > 1) {
				delay *= 2;
				logger.warn("Retrying in {} seconds...", delay / 1000);
				try {
					Thread.sleep(delay);
				} catch (Exception e) {}
			}
			try {
				logger.info("Attempting to connect to {}", msg);
				out = supplier.connect();
				if (out != null)
					break;
			} catch (Exception e) {
				logger.error("Failed to connect to {}: {}", msg, e.getMessage());
			}
		}
		return out;
	}

	private static interface ConnectionSupplier<T> {
		T connect() throws Exception;
	}
}
