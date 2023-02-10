package net.foxgenesis.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.internal.utils.IOUtil;

public abstract class AConnectionProvider implements AutoCloseable {

	protected final Logger logger;
	private final String name;
	protected final Properties properties;

	public AConnectionProvider(Properties properties, String name) {
		this.name = Objects.requireNonNull(name);
		this.logger = LoggerFactory.getLogger(name);
		this.properties = Objects.requireNonNull(properties);

		String type = properties.getProperty("databaseType", "mysql");
		properties.remove("databaseType");
		
		String ip = properties.getProperty("ip", "localhost");
		properties.remove("ip");
		
		String port = properties.getProperty("port", "3306");
		properties.remove("port");
		
		String database = properties.getProperty("database", "WatameBot");
		properties.remove("database");
		
		properties.put("jdbcUrl", "jdbc:%s://%s:%s/%s".formatted(type, ip, port, database));
		
		properties.put("poolName", name);
	}

	protected abstract Connection openConnection() throws SQLException;

	protected <U> U openAutoClosedConnection(ConnectionConsumer<U> consumer, Consumer<Throwable> error) {
		try (Connection conn = openConnection()) {
			return consumer.applyConnection(conn);
		} catch (Exception e) {
			if (error != null)
				error.accept(e);
			return null;
		}
	}

	protected <U> CompletableFuture<U> asyncConnection(Function<Connection, U> function) {
		CompletableFuture<Connection> conn = CompletableFuture.supplyAsync(() -> {
			try {
				return openConnection();
			} catch (SQLException e) {
				throw new CompletionException(e);
			}
		});

		CompletableFuture<Connection> copy = conn.copy();
		return conn.thenApplyAsync(function).whenCompleteAsync((result, error) -> {
			copy.thenAcceptAsync(IOUtil::silentClose);
			if (error != null)
				throw new CompletionException(error);
		});
	}

	public final String getName() { return name; }

	@Override
	public void close() throws Exception {
		logger.debug("Shutting down {}", name);

	}

	@FunctionalInterface
	public interface ConnectionConsumer<U> { public U applyConnection(Connection connection) throws SQLException; }
}