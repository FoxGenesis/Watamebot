package net.foxgenesis.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AConnectionProvider implements AutoCloseable {

	protected final Logger logger;
	protected final Properties properties;

	private final String name;
	private final String database;

	public AConnectionProvider(@NotNull String name, @NotNull Properties properties) {
		this.name = Objects.requireNonNull(name);
		logger = LoggerFactory.getLogger(name);
		this.properties = Objects.requireNonNull(properties);

		String type = properties.getProperty("databaseType", "mysql");
		properties.remove("databaseType");

		String ip = properties.getProperty("ip", "localhost");
		properties.remove("ip");

		String port = properties.getProperty("port", "3306");
		properties.remove("port");

		database = properties.getProperty("database", "WatameBot");
		properties.remove("database");

		properties.put("jdbcUrl", "jdbc:%s://%s:%s/%s".formatted(type, ip, port, database));

		properties.put("poolName", name);
	}

	@NotNull
	protected abstract Connection openConnection() throws SQLException;

	@NotNull
	protected <U> Optional<U> openAutoClosedConnection(@NotNull ConnectionConsumer<U> consumer) throws SQLException {
		try (Connection conn = openConnection()) {
			return Optional.ofNullable(consumer.applyConnection(conn));
		}
	}

	@NotNull
	protected <U> Optional<U> openAutoClosedConnection(@NotNull ConnectionConsumer<U> consumer,
			Consumer<Throwable> error) {
		try (Connection conn = openConnection()) {
			return Optional.ofNullable(consumer.applyConnection(conn));
		} catch (Exception e) {
			if (error != null)
				error.accept(e);
			return Optional.empty();
		}
	}

	public final String getName() {
		return name;
	}

	public String getDatabase() {
		return database;
	}

	@Override
	public void close() throws Exception {
		logger.debug("Shutting down {}", name);

	}

	@FunctionalInterface
	public interface ConnectionConsumer<U> {
		@SuppressWarnings("exports")
		U applyConnection(@NotNull Connection connection) throws SQLException;
	}
}
