package net.foxgenesis.database;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

public class DatabaseManager implements IDatabaseManager {
	@Nonnull
	protected final Logger logger;

	@Nonnull
	private final Set<AbstractDatabase> databases = new HashSet<>();

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
	public boolean register(@Nonnull AbstractDatabase database) throws IOException {
		Objects.requireNonNull(database);

		if (databases.contains(database))
			throw new IllegalArgumentException("Database is already registered!");

		logger.debug("Registering database {}", database.getName());

		boolean wasAdded = false;
		synchronized (databases) {
			wasAdded = databases.add(database);
		}

		if (wasAdded && ready && provider != null) {
			database.setup(provider);
			database.onReady();
		}

		return wasAdded;
	}

	@Override
	public boolean isDatabaseRegistered(AbstractDatabase database) { return databases.contains(database); }

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
				return CompletableFutureUtils
						.allOf(databases.stream().map(database -> CompletableFuture.runAsync(() -> {
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
				return CompletableFutureUtils
						.allOf(databases.stream().map(database -> CompletableFuture.runAsync(() -> {
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
