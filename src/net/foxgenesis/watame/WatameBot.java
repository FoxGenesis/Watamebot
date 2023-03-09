package net.foxgenesis.watame;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.apache.commons.configuration2.ImmutableConfiguration;
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
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.foxgenesis.database.DatabaseManager;
import net.foxgenesis.database.IDatabaseManager;
import net.foxgenesis.database.providers.MySQLConnectionProvider;
import net.foxgenesis.property.IPropertyProvider;
import net.foxgenesis.property.ImmutableProperty;
import net.foxgenesis.util.MethodTimer;
import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.util.resource.ModuleResource;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.PluginHandler;
import net.foxgenesis.watame.property.GuildProperty;
import net.foxgenesis.watame.property.IGuildPropertyMapping;
import net.foxgenesis.watame.property.IGuildPropertyProvider;
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
	 * Singleton instance of class
	 */
	public static final WatameBot INSTANCE;

	private static final WatameBotSettings settings;
	private static final ImmutableConfiguration config;

	static {
		settings = Main.getSettings();
		config = settings.getConfiguration();

		// initialize the main bot object with token
		logger.debug("Creating WatameBot instance");
		INSTANCE = new WatameBot(settings.getToken());
	}

	static void nothing() {}

	/**
	 * Get the singleton instance of {@link WatameBot}.
	 * <p>
	 * If the instance has not been created yet, one will be upon calling this
	 * method.
	 * </p>
	 *
	 * @return Instance of {@link WatameBot}
	 */
	@Deprecated(forRemoval = true, since = "1.0.5")
	public static WatameBot getInstance() { return INSTANCE; }

	// ------------------------------- INSTNACE ====================

	/**
	 * Builder to bulid jda
	 */
	private JDABuilder builder;

	/**
	 * the JDA object
	 */
	private JDA discord;

	/**
	 * Database connection handler
	 */
	private final DatabaseManager manager;

	/**
	 * WatameBot database connection provider
	 */
	private final WatameBotDatabase database;

	/**
	 * Guild logging channel property
	 */
	private final GuildProperty logChannel;

	/**
	 * Current state of the bot
	 */
	private State state = State.CONSTRUCTING;

	/**
	 * Plugin handler
	 */
	private final PluginHandler<Plugin> pluginHandler;
	
	/**
	 * Instance context
	 */
	private final Context context;

	/**
	 * Create a new instance with a specified login {@code token}.
	 *
	 * @param token - Token used to connect to discord
	 * @throws SQLException When failing to connect to the database file
	 */
	private WatameBot(@Nonnull String token) {
		// Update our state
		updateState(State.CONSTRUCTING);

		// Set shutdown thread
		logger.debug("Adding shutdown hook");
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "WatameBot Shutdown Thread"));

		// Create our database manager
		manager = new DatabaseManager("WatameBot Database Manager");

		// Connect to our database file
		database = new WatameBotDatabase();
		logChannel = database.getProperty("log_channel");

		// Create connection to discord through our token
		builder = createJDA(token);
		
		// Set our instance context
		context = new Context(this, builder);

		// Create our plugin handler
		pluginHandler = new PluginHandler<>(context, getClass().getModule().getLayer(), Plugin.class);
	}

	void start() throws Exception {
		long start = System.nanoTime();

		pluginHandler.loadPlugins();

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
		logger.info("Startup completed in " + MethodTimer.formatToSeconds(end - start) + " seconds");

		// Set our state to running
		updateState(State.RUNNING);
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
		
		// Close connection to datebase
		try {
			logger.info("Closing database connection");
			if (database != null)
				database.close();
		} catch (Exception e) {
			logger.error("Error while closing database connection!", e);
		}

		// Disconnect from discord
		if (discord != null) {
			logger.info("Shutting down JDA...");
			discord.shutdown();
		}

		// Await all futures to complete
		if (!ForkJoinPool.commonPool().awaitQuiescence(1, TimeUnit.MINUTES))
			logger.warn("Timed out waiting for common pool shutdown. Continuing shutdown...");

		logger.info("Exiting...");
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("resource")
	private void preInit() throws Exception {
		/*
		 * ====== PRE-INITIALIZATION ======
		 */

		// Pre-initialize all plugins async
		CompletableFuture<Void> pluginPreInit = pluginHandler.preInit();

		// Setup the database
		try {
			logger.debug("Adding database to database manager");
			manager.register(pluginHandler.getPlugin("integrated"), database);
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
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @throws Throwable
	 */
	@SuppressWarnings("resource")
	private void init() throws Exception {
		/*
		 * ====== INITIALIZATION ======
		 */

		// Initialize all plugins
		CompletableFuture<Void> pluginInit = pluginHandler.init();

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

		logger.debug("Waiting for plugin initialization");
		pluginInit.join();
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @throws Throwable
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
		pluginHandler.onReady(this);

		// Display our game as ready
		logger.debug("Setting presence to ready");
		discord.getPresence().setPresence(OnlineStatus.ONLINE,
				Activity.playing(config.getString("status.online", "Watame 2!")));
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
						GatewayIntent.MESSAGE_CONTENT)
				.disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER,
						CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
				.setChunkingFilter(ChunkingFilter.ALL).setAutoReconnect(true)
				.setActivity(Activity.playing(config.getString("status.startup", "Initalizing...")))
				.setMemberCachePolicy(MemberCachePolicy.ALL).setStatus(OnlineStatus.DO_NOT_DISTURB);

		builder.addEventListeners(new ListenerAdapter() {
			@Override
			public void onGuildReady(@Nonnull GuildReadyEvent e) { database.addGuild(e.getGuild()); }

			@Override
			public void onGuildLeave(@Nonnull GuildLeaveEvent e) { database.removeGuild(e.getGuild()); }
		});

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

	@Deprecated(forRemoval = true)
	public IGuildData getDataForGuild(Guild guild) { return database.getDataForGuild(guild); }

	/**
	 * Get the property provider instance.
	 * 
	 * @return The current {@link IPropertyProvider} instance
	 */
	public IGuildPropertyProvider getPropertyProvider() { return database; }

	/**
	 * Get the logging channel property for a guild.
	 * 
	 * @return Returns the {@link ImmutableProperty} pointing to the log channel
	 */
	public ImmutableProperty<String, Guild, IGuildPropertyMapping> getGuildLoggingChannel() { return logChannel; }

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

	private void updateState(State state) {
		this.state = state;
		logger.trace("STATE = " + state);
		MDC.put("watame.status", state.name());
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

		State() { marker = MarkerFactory.getMarker(this.name()); }
	}
}
