package net.foxgenesis.watame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.foxgenesis.database.DatabaseManager;
import net.foxgenesis.database.IDatabaseManager;
import net.foxgenesis.database.providers.MySQLConnectionProvider;
import net.foxgenesis.property.IPropertyProvider;
import net.foxgenesis.util.ProgramArguments;
import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.util.ResourceUtils.ModuleResource;
import net.foxgenesis.watame.plugin.IPlugin;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.plugin.UntrustedPluginLoader;
import net.foxgenesis.watame.property.GuildPropertyProvider;
import net.foxgenesis.watame.property.IGuildPropertyMapping;
import net.foxgenesis.watame.sql.IGuildData;
import net.foxgenesis.watame.sql.WatameBotDatabase;

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
	 * Directory for configuration files
	 */
	public static final File CONFIG_DIR = new File(System.getProperty("watame.config_dir", "config/"));

	/**
	 * Singleton instance of class
	 */
	private static WatameBot instance;

	/**
	 * Variable stating if instance has been created
	 */
	private static boolean toInit = true;

	/**
	 * Get the singleton instance of {@link WatameBot}.
	 * <p>
	 * If the instance has not been created yet, one will be upon calling this
	 * method.
	 * </p>
	 *
	 * @return Instance of {@link WatameBot}
	 */
	public static WatameBot getInstance() {
		if (toInit) {
			synchronized (WatameBot.class) {
				if (toInit) {
					ProgramArguments params = Main.getProgramArguments();
					// Check if the token parameter was passed in
					if (!params.hasParameter("token"))
						ExitCode.NO_TOKEN.programExit("No token file specified");

					// Get discord login token from file
					String token = Objects.requireNonNull(readToken(params.getParameter("token")));

					try {
						// initialize the main bot object with token
						logger.debug("Creating WatameBot instance");
						instance = new WatameBot(token);

						toInit = false;
					} catch (SQLException e) {
						ExitCode.DATABASE_NOT_CONNECTED.programExit(e);
						return null;
					}
				}
			}
		}

		return instance;
	}

	/**
	 * NEED_JAVADOC
	 *
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

	// ------------------------------- INSTNACE ====================

	private JDABuilder builder;
	/**
	 * the JDA object
	 */
	private JDA discord;

	private final DatabaseManager manager;

	/**
	 * Database connection handler
	 */
	private final WatameBotDatabase database;

	/**
	 * Property provider
	 */
	private final GuildPropertyProvider provider;

	/**
	 * Current state of the bot
	 */
	private State state = State.CONSTRUCTING;

	/**
	 * Plugin loader
	 */
	private final UntrustedPluginLoader<IPlugin> loader;

	/**
	 * List of all plugins
	 */
	private ConcurrentHashMap<String, IPlugin> plugins = new ConcurrentHashMap<>();

	/**
	 * Create a new instance with a specified login {@code token}.
	 *
	 * @param token - Token used to connect to discord
	 * @throws SQLException When failing to connect to the database file
	 */
	private WatameBot(@Nonnull String token) throws SQLException {
		// Update our state
		updateState();

		// Set shutdown thread
		logger.debug("Adding shutdown hook");
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "WatameBot Shutdown Thread"));

		if (!createConfigurationDirectory())
			ExitCode.SETUP_ERROR.programExit(
					"Failed to create configuration directory. Does a file with the same name already exist?");

		// Load plugins
		loader = new UntrustedPluginLoader<>(IPlugin.class);

		// Create our database manager
		manager = new DatabaseManager("WatameBot Database Manager");

		// Connect to our database file
		database = new WatameBotDatabase();

		// Create connection to discord through our token
		builder = createJDA(token);

		// Create our property provider
		provider = new GuildPropertyProvider(database);
	}

	void start() {
		logger.info("Starting...");
		loader.getPlugins().forEach(plugin -> plugins.put(plugin.getName(), plugin));
		logger.debug("Found {} plugins", plugins.size());

		preInit();
	}

	/**
	 * Bot shutdown method.
	 * <p>
	 * This method will be called on program exit.
	 * </p>
	 */
	private void shutdown() {
		// Set our state to shutdown
		state = State.SHUTDOWN;
		updateState();

		logger.info("Shutting down...");

		// Close all plugins
		logger.debug("Closing all pugins");
		plugins.values().forEach(plugin -> IOUtil.silentClose(plugin));

		// Await all futures to complete
		if (!ForkJoinPool.commonPool().awaitQuiescence(3, TimeUnit.MINUTES))
			logger.warn("Timed out waiting for pool shutdown. Continuing shutdown...");

		// Disconnect from discord
		if (discord != null) {
			logger.info("Shutting down JDA...");
			discord.shutdown();
		}

		// Close connection to datebase
		try {
			logger.info("Closing database connection");
			if (database != null)
				database.close();
		} catch (Exception e) {
			logger.error("Error while closing database connection!", e);
		}

		logger.info("Exiting...");
	}

	/**
	 * NEED_JAVADOC
	 */
	private void preInit() {
		// Set our state to pre-init
		state = State.PRE_INIT;
		logger.trace("STATE = " + state);
		updateState();

		/*
		 * ====== PRE-INITIALIZATION ======
		 */

		// Pre-initialize all plugins async
		logger.debug("Calling plugin pre-initialization async");
		CompletableFuture<Void> pluginPreInit = CompletableFuture.allOf(plugins.values().stream()
				.map(plugin -> CompletableFuture.runAsync(plugin::preInit).exceptionallyAsync(error -> {
					pluginError(plugin, error, state.marker);
					return null;
				})).toArray(CompletableFuture[]::new));

		// Setup the database
		try {
			logger.debug("Adding database to database manager");
			manager.register(database);
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
		logger.trace("Waiting for plugin pre-initialization");
		pluginPreInit.join();

		init();
	}

	/**
	 * NEED_JAVADOC
	 */
	private void init() {
		// Set our state to init
		state = State.INIT;
		logger.trace("STATE = " + state);
		updateState();

		/*
		 * ====== INITIALIZATION ======
		 */

		// Initialize all plugins
		logger.debug("Calling plugin initialization async");
		ProtectedJDABuilder pBuilder = new ProtectedJDABuilder(builder);
		CompletableFuture<Void> pluginInit = CompletableFuture.allOf(plugins.values().stream()
				.map(plugin -> CompletableFuture.runAsync(() -> plugin.init(pBuilder)).exceptionallyAsync(error -> {
					pluginError(plugin, error, state.marker);
					return null;
				})).toArray(CompletableFuture[]::new));

		// Start databases
		try {
			logger.info("Starting database pool");
			manager.start(
					new MySQLConnectionProvider(ResourceUtils.getProperties(Path.of("config", "database.properties"),
							new ModuleResource("watamebot", "defaults/database.properties"))))
					.join();
		} catch (IOException e) {
			// Some error occurred while setting up database
			ExitCode.DATABASE_SETUP_ERROR.programExit(e);
		}

		/*
		 * ====== END INITIALIZATION ======
		 */

		logger.trace("Waiting for plugin initialization");
		pluginInit.join();

		postInit();
	}

	/**
	 * NEED_JAVADOC
	 */
	private void postInit() {
		// Set our state to post-init
		state = State.POST_INIT;
		logger.trace("STATE = " + state);
		updateState();

		/*
		 * ====== POST-INITIALIZATION ======
		 */
		logger.trace("Building discord connection");
		discord = buildJDA();

		// Post-initialize all plugins
		logger.debug("Calling plugin post-initialization async");
		CompletableFuture<Void> pluginPostInit = CompletableFuture.allOf(plugins.values().stream()
				.map(plugin -> CompletableFuture.runAsync(() -> plugin.postInit(this)).exceptionallyAsync(error -> {
					pluginError(plugin, error, state.marker);
					return null;
				})).toArray(CompletableFuture[]::new));

		// Register commands
		logger.trace("Collecting command data");
		CommandListUpdateAction update = discord.updateCommands();
		plugins.values().stream().filter(IPlugin::providesCommands)
				.forEach(plugin -> update.addCommands(plugin.getCommands()));
		update.queue();

		/*
		 * ====== END POST-INITIALIZATION ======
		 */

		logger.trace("Waiting for plugin post-initialization");
		pluginPostInit.join();

		// Wait for discord to be ready
		if (discord.getStatus() != Status.CONNECTED)
			try {
				// Wait for JDA to be ready for use (BLOCKING!).
				logger.info("Waiting for JDA to be ready...");
				discord.awaitReady();
			} catch (InterruptedException e) {}
		logger.info("Connected to discord!");

		ready();
	}

	private void ready() {
		// Set our state to running
		state = State.RUNNING;
		logger.trace("STATE = " + state);
		updateState();

		logger.debug("Calling plugin on ready async");
		CompletableFuture.allOf(plugins.values().stream()
				.map(plugin -> CompletableFuture.runAsync(() -> plugin.onReady(this)).exceptionallyAsync(error -> {
					pluginError(plugin, error, state.marker);
					return null;
				})).toArray(CompletableFuture[]::new));

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
	private JDABuilder createJDA(String token) {
		Objects.requireNonNull(token, "Login token must not be null");

		// Setup our JDA with wanted values
		logger.debug("Creating JDA");
		JDABuilder builder = JDABuilder
				.create(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_BANS, GatewayIntent.GUILD_MESSAGES,
						GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES,
						GatewayIntent.MESSAGE_CONTENT)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER,
						CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
				.setChunkingFilter(ChunkingFilter.NONE).setAutoReconnect(true)
				.setActivity(Activity.playing("Initializing...")).setStatus(OnlineStatus.DO_NOT_DISTURB);

		builder.addEventListeners(new ListenerAdapter() {
			@Override
			public void onGuildReady(@Nonnull GuildReadyEvent e) { database.addGuild(e.getGuild()); }

			@Override
			public void onGuildLeave(@Nonnull GuildLeaveEvent e) { database.removeGuild(e.getGuild()); }
		});

		return builder;
	}

	private JDA buildJDA() {
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
			} catch (LoginException ex) {
				// Failed to connect. Log error
				logger.warn("Failed to connect: " + ex.getLocalizedMessage() + " retrying...", ex);

				// Sleep for one second before
				try {
					Thread.sleep(10000);
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
	 * Remove a plugin from the managed plugins, closing its resources in the
	 * process.
	 * 
	 * @param plugin - the plugin to unload
	 */
	private void unloadPlugin(IPlugin plugin) {
		logger.trace("Unloading {}", plugin.getClass());
		IOUtil.silentClose(plugin);
		plugins.remove(plugin.getName());
		logger.warn(plugin.getClass() + " unloaded");
	}

	/**
	 * Indicate that a plugin has thrown an error during one of its initialization
	 * methods.
	 * 
	 * @param plugin - plugin in question
	 * @param error  - the error that was thrown
	 * @param marker - method marker
	 */
	private void pluginError(IPlugin plugin, Throwable error, Marker marker) {
		Throwable temp = error;

		if (error instanceof CompletionException && error.getCause() instanceof SeverePluginException)
			temp = error.getCause();

		if (temp instanceof SeverePluginException) {
			SeverePluginException pluginException = (SeverePluginException) temp;

			Marker m = MarkerFactory.getMarker(pluginException.isFatal() ? "FATAL" : "SEVERE");
			m.add(marker);

			logger.error(m, "Exception in " + plugin.getName(), pluginException);

			if (pluginException.isFatal())
				unloadPlugin(plugin);
		} else
			logger.error(marker, "Error in " + plugin.getName(), error);
	}

	/**
	 * Check if this instances {@link JDA} is built and connected to Discord.
	 *
	 * @return {@link JDA} instance is built and its current status is
	 *         {@link Status#CONNECTED}.
	 */
	public boolean isConnectedToDiscord() { return discord != null && discord.getStatus() == Status.CONNECTED; }

	/**
	 * NEED_JAVADOC
	 *
	 * @return
	 */
	public IDatabaseManager getDatabaseManager() { return manager; }

	public IGuildData getDataForGuild(Guild guild) { return database.getDataForGuild(guild); }

	/**
	 * Get the property provider instance.
	 * 
	 * @return The current {@link IPropertyProvider} instance
	 */
	public IPropertyProvider<String, Guild, IGuildPropertyMapping> getPropertyProvider() { return provider; }

	/**
	 * NEED_JAVADOC
	 *
	 * @return
	 */
	public JDA getJDA() { return discord; }

	/**
	 * Get the current state of the bot.
	 *
	 * @return Returns the {@link State} of the bot
	 * @see State
	 */
	public State getState() { return state; }

	private void updateState() { MDC.put("watame.status", state.name()); }

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

		State() { marker = MarkerFactory.getMarker(this.name()); }
	}

	private static boolean createConfigurationDirectory() {
		if (!CONFIG_DIR.exists())
			return CONFIG_DIR.mkdirs();
		return CONFIG_DIR.isDirectory();
	}
}
