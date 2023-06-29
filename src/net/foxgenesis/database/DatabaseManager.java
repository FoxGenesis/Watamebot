package net.foxgenesis.database;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.internal.utils.IOUtil;
import net.foxgenesis.util.CompletableFutureUtils;
import net.foxgenesis.util.MethodTimer;
import net.foxgenesis.watame.plugin.Plugin;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
public class DatabaseManager implements IDatabaseManager, AutoCloseable {
	@NotNull
	protected final Logger logger;

	// @NotNull
	// private final Set<AbstractDatabase> databases = new HashSet<>();

	private final HashMap<Plugin, Set<AbstractDatabase>> databases = new HashMap<>();

	@NotNull
	private final String name;

	@Nullable
	private AConnectionProvider provider;

	private boolean ready = false;

	/**
	 * NEED_JAVADOC
	 * 
	 * @param name
	 */
	public DatabaseManager(@NotNull String name) {
		this.name = Objects.requireNonNull(name);
		logger = LoggerFactory.getLogger(name);
	}

	@SuppressWarnings("resource")
	@Override
	public boolean register(@NotNull Plugin plugin, @NotNull AbstractDatabase database) throws IOException {
		Objects.requireNonNull(database);

		if (!plugin.needsDatabase)
			throw new IllegalArgumentException("Plugin does not declare that it needs database connection!");

		if (isDatabaseRegistered(database))
			throw new IllegalArgumentException("Database is already registered!");

		logger.debug("Registering database {}", database.getName());

		boolean wasAdded = false;
		synchronized (databases) {
			databases.putIfAbsent(plugin, new HashSet<>());
			wasAdded = databases.get(plugin).add(database);
		}

		if (wasAdded && ready && provider != null) {
			database.setup(provider);
			database.onReady();
		}

		return wasAdded;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param owner
	 * 
	 * @return
	 */
	public boolean unload(Plugin owner) {
		if (!owner.needsDatabase)
			return false;

		if (databases.containsKey(owner)) {
			synchronized (databases) {
				if (databases.containsKey(owner)) {
					logger.info("Unloading databases from {}", owner.friendlyName);
					Set<AbstractDatabase> databases = this.databases.remove(owner);

					for (AbstractDatabase database : databases)
						database.unload();

					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isDatabaseRegistered(AbstractDatabase database) {
		return databases.values().stream().anyMatch(set -> set.contains(database));
	}

	public synchronized CompletableFuture<Void> start(@NotNull AConnectionProvider provider, Executor executor) {
		final Executor ex = executor == null? ForkJoinPool.commonPool() : executor;
		long start = System.nanoTime();
		return CompletableFuture.runAsync(() -> {
			this.provider = Objects.requireNonNull(provider);
			logger.info("Starting {} using provider {}", name, provider.getName());

			logger.debug("Collecting database setup lines");
			List<String> setupLines = collectDatabaseSetupLines();

			synchronized (databases) {
				provider.openAutoClosedConnection(connection -> {
					try (Statement statement = connection.createStatement()) {
						for (String line : setupLines)
							try {
								statement.execute(line);
							} catch (SQLException e) {
								logger.error("Error executing database setup line [" + line + "]", e);
							}
					}
					return null;
				}, error -> logger.error("Error while setting up database tables", error));
			}
		}, ex).thenComposeAsync((v) -> {
			synchronized (databases) {
				return CompletableFutureUtils.allOf(databases.values().stream().flatMap(Set::stream)
						.map(database -> CompletableFuture.runAsync(() -> {
							try {
								database.setup(provider);
							} catch (IOException e) {
								throw new CompletionException(e);
							}
						}, ex).exceptionally(e -> {
							logger.error("Error while setting up " + database.getName(), e);
							return null;
						})));
			}
		}, ex).thenComposeAsync((v) -> {
			logger.debug("Calling database on ready");
			ready = true;
			synchronized (databases) {
				return CompletableFutureUtils.allOf(databases.values().stream().flatMap(Set::stream)
						.map(database -> CompletableFuture.runAsync(() -> {
							database.onReady();
						}, ex).exceptionally(e -> {
							logger.error("Error while setting up " + database.getName(), e);
							return null;
						})));
			}
		}, ex).whenComplete((v, err) -> logger.debug("Startup completed in {} seconds",
				MethodTimer.formatToSeconds(System.nanoTime() - start)));
	}

	public synchronized void start(@NotNull AConnectionProvider provider) {
		long start = System.nanoTime();

		this.provider = Objects.requireNonNull(provider);
		logger.info("Starting {} using provider {}", name, provider.getName());

		synchronized (databases) {
			logger.debug("Collecting database setup lines");
			List<String> setupLines = collectDatabaseSetupLines();

			provider.openAutoClosedConnection(connection -> {
				try (Statement statement = connection.createStatement()) {
					for (String line : setupLines)
						try {
							statement.execute(line);
						} catch (SQLException e) {
							logger.error("Error executing database setup line [" + line + "]", e);
						}
				}
				return null;
			}, error -> logger.error("Error while setting up database tables", error));

			databases.values().stream().flatMap(Set::stream).map(database -> {
				try {
					database.setup(provider);
					return database;
				} catch (IOException e) {
					logger.error("Error while setting up " + database.getName(), e);
					return null;
				}
			}).filter(Objects::nonNull).forEach(AbstractDatabase::onReady);
			ready = true;
		}
		logger.debug("Startup completed in {} seconds", MethodTimer.formatToSeconds(System.nanoTime() - start));
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	@Override
	@NotNull
	public String getName() {
		return name;
	}

	@Override
	public void close() throws Exception {
		databases.values().forEach(s -> s.forEach(IOUtil::silentClose));
		if (provider != null)
			provider.close();
	}

	private List<String> collectDatabaseSetupLines() {
		List<String> lines = new ArrayList<>();

		synchronized (databases) {
			for (Set<AbstractDatabase> databases : this.databases.values())
				for (AbstractDatabase database : databases)
					try {
						for (String line : database.getSetupLines())
							lines.add(line);
					} catch (IOException e) {
						logger.error("Error while reading setup lines from " + database.getName(), e);
					}
		}

		return lines;
	}
}
