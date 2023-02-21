package net.foxgenesis.database;

import java.io.IOException;
import java.net.ConnectException;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.util.CompletableFutureUtils;
import net.foxgenesis.watame.plugin.Plugin;

public class DatabaseManager implements IDatabaseManager {
	@Nonnull
	protected final Logger logger;

	// @Nonnull
	// private final Set<AbstractDatabase> databases = new HashSet<>();

	private final HashMap<Plugin, Set<AbstractDatabase>> databases = new HashMap<>();

	@Nonnull
	private final String name;

	@Nullable
	private AConnectionProvider provider;

	private boolean ready = false;

	public DatabaseManager(@Nonnull String name) {
		this.name = Objects.requireNonNull(name);
		logger = LoggerFactory.getLogger(name);
	}

	@Override
	public boolean register(@Nonnull Plugin plugin, @Nonnull AbstractDatabase database) throws IOException {
		Objects.requireNonNull(database);

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

	/**
	 * NEED_JAVADOC
	 * 
	 * @param provider
	 * @return
	 * @throws ConnectException
	 */
	public CompletableFuture<Void> start(@Nonnull AConnectionProvider provider) throws ConnectException {
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
		}).thenCompose((v) -> {
			synchronized (databases) {
				return CompletableFutureUtils.allOf(databases.values().stream().flatMap(Set::stream)
						.map(database -> CompletableFuture.runAsync(() -> {
							try {
								database.setup(provider);
							} catch (IOException e) {
								throw new CompletionException(e);
							}
						}).exceptionally(e -> {
							logger.error("Error while setting up " + database.getName(), e);
							return null;
						})));
			}
		}).thenRun(() -> ready = true).thenCompose((v) -> {
			synchronized (databases) {
				return CompletableFutureUtils.allOf(databases.values().stream().flatMap(Set::stream)
						.map(database -> CompletableFuture.runAsync(() -> {
							database.onReady();
						}).exceptionally(e -> {
							logger.error("Error while setting up " + database.getName(), e);
							return null;
						})));
			}
		});
	}

	@Override
	public boolean isReady() { return ready; }

	@Override
	@Nonnull
	public String getName() { return name; }

	@Override
	public void close() {}

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
