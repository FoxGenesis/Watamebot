package net.foxgenesis.watame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
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
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.foxgenesis.util.ProgramArguments;
import net.foxgenesis.watame.plugin.IPlugin;
import net.foxgenesis.watame.plugin.UntrustedPluginLoader;
import net.foxgenesis.watame.sql.DataManager;
import net.foxgenesis.watame.sql.DataManager.DatabaseLoadedEvent;
import net.foxgenesis.watame.sql.IDatabaseManager;

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
	public static Logger logger = LoggerFactory.getLogger(WatameBot.class);

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
					String token = readToken(params.getParameter("token"));

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
	 * Plugin loader
	 */
	private final UntrustedPluginLoader<IPlugin> loader;

	/**
	 * List of all plugins
	 */
	private Collection<IPlugin> plugins;

	/**
	 * Create a new instance with a specified login {@code token}.
	 * 
	 * @param token - Token used to connect to discord
	 * @throws SQLException When failing to connect to the database file
	 */
	private WatameBot(@Nonnull String token) throws SQLException {
		Objects.requireNonNull(token);

		// Set shutdown thread
		logger.debug("Adding shutdown hook");
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "WatameBot Shutdown Thread"));

		// Load plugins
		loader = new UntrustedPluginLoader<>(IPlugin.class, Constants.pluginFolder, true);
		plugins = loader.getPlugins();

		// Connect to our database file
		database = new DataManager();

		// Create connection to discord through our token
		discord = createJDA(token);
	}

	/**
	 * NEED_JAVADOC
	 */
	void preInit() {
		// Set our state to pre-init
		state = State.PRE_INIT;
		logger.trace("STATE = " + state);

		// Pre-initialize all plugins
		Thread pluginPreInit = new Thread(() -> plugins.parallelStream().forEach(IPlugin::preInit),
				"Plugin pre-init thread");
		pluginPreInit.start();

		// Setup and connect to the database
		databaseInit();

		// Wait for all plugins to be have pre-initialized
		try {
			logger.trace("Waiting for plugin pre-init thread");
			pluginPreInit.join();
		} catch (InterruptedException e) {}

		waitUntilReady();
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

		// Get all database data
		logger.trace("Retrieving all data");
		database.retrieveDatabaseData(discord);
		discord.getEventManager().handle(new DatabaseLoadedEvent(discord, database));
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
			} catch (InterruptedException e) {}
		logger.info("Connected to discord!");
	}

	/**
	 * NEED_JAVADOC
	 */
	void init() {
		// Set our state to init
		state = State.INIT;
		logger.trace("STATE = " + state);

		// Initialize all plugins
		plugins.parallelStream().forEach(plugin -> plugin.init(this));
	}

	/**
	 * NEED_JAVADOC
	 */
	void postInit() {
		// Set our state to post-init
		state = State.POST_INIT;
		logger.trace("STATE = " + state);

		// Post-initialize all plugins
		plugins.forEach(plugin -> plugin.postInit(this));

		// Get global and guild interactions FIXME re-implement interactions
		/*
		 * logger.trace("Getting integrations"); SnowflakeCacheView<Guild> guildCache =
		 * discord.getGuildCache();
		 * 
		 * Collection<CommandData> interactions = plugins.parallelStream() .map(plugin
		 * -> ((InteractionHandler)
		 * plugin.getInteractionHandler()).getAllInteractions(guildCache))
		 * .filter(cmdData -> cmdData != null).reduce((a, b) -> { a.addAll(b); return a;
		 * }).orElse(new ArrayList<CommandData>());
		 * 
		 * 
		 * logger.info("Adding {} integrations", interactions.size());
		 * discord.updateCommands().addCommands(interactions).queue();
		 */

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
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER,
						CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
				.setChunkingFilter(ChunkingFilter.NONE).setAutoReconnect(true)
				.setActivity(Activity.playing("Initializing...")).setStatus(OnlineStatus.DO_NOT_DISTURB);

		builder.addEventListeners(new ListenerAdapter() {
			@Override
			public void onGuildReady(GuildReadyEvent e) { database.addGuild(e.getGuild()); }

			@Override
			public void onGuildLeave(GuildLeaveEvent e) { database.removeGuild(e.getGuild()); }
		});

		ProtectedJDABuilder pBuilder = new ProtectedJDABuilder(builder);
		plugins.forEach(plugin -> plugin._construct(pBuilder));

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
	public boolean isConnectedToDiscord() { return discord != null && discord.getStatus() == Status.CONNECTED; }

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public IDatabaseManager getDatabase() { return database; }

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

	/**
	 * States {@link WatameBot} goes through on startup.
	 * 
	 * @author Ashley
	 */
	public enum State {
		// NEED_JAVADOC javadoc needed for enum
		CONSTRUCTING, PRE_INIT, INIT, POST_INIT, RUNNING
	}

	public class ProtectedJDABuilder {

		private final JDABuilder builder;

		ProtectedJDABuilder(JDABuilder builder) { this.builder = builder; }

		/**
		 * Adds all provided listeners to the list of listeners that will be used to
		 * populate the {@link net.dv8tion.jda.api.JDA JDA} object. <br>
		 * This uses the {@link net.dv8tion.jda.api.hooks.InterfacedEventManager
		 * InterfacedEventListener} by default. <br>
		 * To switch to the {@link net.dv8tion.jda.api.hooks.AnnotatedEventManager
		 * AnnotatedEventManager}, use
		 * {@link #setEventManager(net.dv8tion.jda.api.hooks.IEventManager)
		 * setEventManager(new AnnotatedEventManager())}.
		 *
		 * <p>
		 * <b>Note:</b> When using the
		 * {@link net.dv8tion.jda.api.hooks.InterfacedEventManager
		 * InterfacedEventListener} (default), given listener(s) <b>must</b> be instance
		 * of {@link net.dv8tion.jda.api.hooks.EventListener EventListener}!
		 *
		 * @param listeners The listener(s) to add to the list.
		 *
		 * @throws java.lang.IllegalArgumentException If either listeners or one of it's
		 *                                            objects is {@code null}.
		 *
		 * @return The JDABuilder instance. Useful for chaining.
		 *
		 * @see net.dv8tion.jda.api.JDA#addEventListener(Object...)
		 *      JDA.addEventListener(Object...)
		 */
		@Nonnull
		public ProtectedJDABuilder addEventListeners(@Nonnull Object... listeners) {
			builder.addEventListeners(listeners);
			return this;
		}

		/**
		 * Removes all provided listeners from the list of listeners.
		 *
		 * @param listeners The listener(s) to remove from the list.
		 *
		 * @throws java.lang.IllegalArgumentException If either listeners or one of it's
		 *                                            objects is {@code null}.
		 *
		 * @return The JDABuilder instance. Useful for chaining.
		 *
		 * @see net.dv8tion.jda.api.JDA#removeEventListener(Object...)
		 *      JDA.removeEventListener(Object...)
		 */
		@Nonnull
		public ProtectedJDABuilder removeEventListeners(@Nonnull Object... listeners) {
			builder.removeEventListeners(listeners);
			return this;
		}
	}
}
