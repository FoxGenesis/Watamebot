package net.foxgenesis.watame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.foxgenesis.property.IPropertyProvider;
import net.foxgenesis.util.ProgramArguments;
import net.foxgenesis.watame.command.ConfigCommand;
import net.foxgenesis.watame.command.PingCommand;
import net.foxgenesis.watame.plugin.IPlugin;
import net.foxgenesis.watame.plugin.UntrustedPluginLoader;
import net.foxgenesis.watame.property.GuildPropertyProvider;
import net.foxgenesis.watame.property.IGuildPropertyMapping;
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
	 * General purpose logger
	 */
	public static final Logger logger = LoggerFactory.getLogger(WatameBot.class);

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

	/**
	 * Database connection handler
	 */
	private final DataManager database;

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
	private Collection<IPlugin> plugins;

	/**
	 * Create a new instance with a specified login {@code token}.
	 *
	 * @param token - Token used to connect to discord
	 * @throws SQLException When failing to connect to the database file
	 */
	private WatameBot(@Nonnull String token) throws SQLException {
		// Set shutdown thread
		logger.debug("Adding shutdown hook");
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "WatameBot Shutdown Thread"));

		// Load plugins
		loader = new UntrustedPluginLoader<>(IPlugin.class);

		// Connect to our database file
		database = new DataManager();

		// Create connection to discord through our token
		builder = createJDA(token);

		// Create our property provider
		provider = new GuildPropertyProvider(database);
	}

	void start() {
		logger.info("Starting...");

		plugins = loader.getPlugins();
		logger.debug("Found {} plugins", plugins.size());

		preInit();
	}

	/**
	 * NEED_JAVADOC
	 */
	private void preInit() {
		// Set our state to pre-init
		state = State.PRE_INIT;
		logger.trace("STATE = " + state);

		/*
		 * ====== PRE-INITIALIZATION ======
		 */

		// Pre-initialize all plugins async
		logger.debug("Calling plugin pre-initialization async");
		CompletableFuture<Void> pluginPreInit = CompletableFuture.allOf(plugins.stream()
				.map(plugin -> CompletableFuture.runAsync(plugin::preInit)).toArray(CompletableFuture[]::new));

		// Setup and connect to the database
		databaseInit();

		/*
		 * ====== END PRE-INITIALIZATION ======
		 */

		// Wait for all plugins to be have pre-initialized
		logger.trace("Waiting for plugin pre-initialization");
		pluginPreInit.join();

		init();
	}

	/**
	 * Setup and connect to the database
	 */
	private void databaseInit() {
		// Setup database with a specific resource
		try {
			logger.debug("Connecting to database");
			database.connect();
			database.retrieveDatabaseData(null);
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
	}

	/**
	 * NEED_JAVADOC
	 */
	private void init() {
		// Set our state to init
		state = State.INIT;
		logger.trace("STATE = " + state);

		/*
		 * ====== INITIALIZATION ======
		 */

		// Initialize all plugins
		logger.debug("Calling plugin initialization async");
		ProtectedJDABuilder pBuilder = new ProtectedJDABuilder(builder);
		CompletableFuture<Void> pluginInit = CompletableFuture
				.allOf(plugins.stream().map(plugin -> CompletableFuture.runAsync(() -> plugin.init(pBuilder)))
						.toArray(CompletableFuture[]::new));

		pBuilder.addEventListeners(new PingCommand(), new ConfigCommand());

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

		/*
		 * ====== POST-INITIALIZATION ======
		 */

		discord = buildJDA();
		// Post-initialize all plugins
		logger.debug("Calling plugin post-initialization async");
		CompletableFuture<Void> pluginPostInit = CompletableFuture
				.allOf(plugins.stream().map(plugin -> CompletableFuture.runAsync(() -> plugin.postInit(this)))
						.toArray(CompletableFuture[]::new));

		// Register default commands
		discord.upsertCommand(Commands.slash("ping", "Ping the bot to test the connection"))
				.and(discord.upsertCommand(Commands.slash("config-get", "Get the configuration of the bot")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
						.setGuildOnly(true)
						.addOption(OptionType.STRING, "key", "Location of the variable", true, false)))
				.and(discord.upsertCommand(Commands.slash("config-set", "Get the configuration of the bot")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
						.setGuildOnly(true).addOption(OptionType.STRING, "key", "Location of the variable", true, false)
						.addOption(OptionType.STRING, "type", "The variable's type", true, true)
						.addOptions(createAllOptions())))
				.queue();

		/*
		 * ====== END POST-INITIALIZATION ======
		 */

		logger.trace("Waiting for plugin post-initialization");
		pluginPostInit.join();

		waitUntilReady();

		// Display our game as ready
		logger.debug("Setting presence to ready");
		discord.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("type <help>"));

		// Set our state to running
		state = State.RUNNING;
		logger.trace("STATE = " + state);

		logger.debug("Calling plugin on ready async");
		CompletableFuture.allOf(plugins.stream().map(plugin -> CompletableFuture.runAsync(() -> plugin.onReady(this)))
				.toArray(CompletableFuture[]::new));
	}

	/**
	 * Get the property provider instance.
	 * 
	 * @return The current {@link IPropertyProvider} instance
	 */
	public IPropertyProvider<String, Guild, IGuildPropertyMapping> getPropertyProvider() { return provider; }

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
		RUNNING
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
		 * {@link JDA#setEventManager(net.dv8tion.jda.api.hooks.IEventManager)
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

	private static List<OptionData> createAllOptions() {
		OptionType[] values = OptionType.values();
		List<OptionData> list = new ArrayList<>(values.length-3);
		for (OptionType type : values)
			if (!(type == OptionType.UNKNOWN || type == OptionType.SUB_COMMAND || type == OptionType.SUB_COMMAND_GROUP))
				list.add(new OptionData(type, type.name().toLowerCase(), "Value to set of type " + type.name().toLowerCase()).setAutoComplete(false)
						.setRequired(false));
		return list;
	}
}
