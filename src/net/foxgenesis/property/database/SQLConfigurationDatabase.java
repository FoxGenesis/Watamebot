package net.foxgenesis.property.database;

import java.sql.ResultSet;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
public class SQLConfigurationDatabase extends ConfigurationDatabase<Long> {

	/**
	 * NEED_JAVADOC
	 * 
	 * @param name
	 * @param database
	 * @param table
	 */
	public SQLConfigurationDatabase(String name, String database, String table) {
		super(name, database, table);
	}

	@Override
	protected Optional<String> getInternal(@NotNull Long lookup, @NotNull String key) {
		if (!isReady())
			throw new UnsupportedOperationException("Database is not ready yet!");

		Objects.requireNonNull(lookup);
		Objects.requireNonNull(key);

		return this.mapStatement("property_get", statement -> {
			statement.setLong(1, lookup);
			statement.setString(2, key);

			try (ResultSet result = statement.executeQuery()) {
				return result.next() ? result.getString("property") : null;
			}
		}, err -> logger.error("Error while getting internal property", err));
	}

	@Override
	protected boolean putInternal(@NotNull Long lookup, @NotNull String key, @NotNull String value) {
		if (!isReady())
			throw new UnsupportedOperationException("Database is not ready yet!");

		Objects.requireNonNull(lookup);
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);

		return this.mapStatement("property_insert", statement -> {
			statement.setLong(1, lookup);
			statement.setString(2, key);
			statement.setString(3, value);
			return statement.executeUpdate() > 0;
		}, err -> logger.error("Error while putting internal property", err)).orElse(false);
	}

	@Override
	protected boolean removeInternal(@NotNull Long lookup, @NotNull String key) {
		if (!isReady())
			throw new UnsupportedOperationException("Database is not ready yet!");

		Objects.requireNonNull(key);

		return this.mapStatement("property_delete", statement -> {
			statement.setLong(1, lookup);
			statement.setString(2, key);
			return statement.executeUpdate() > 0;
		}, err -> logger.error("Error while removing internal property", err)).orElse(false);
	}

	@Override
	protected void onReady() {}

	@Override
	public synchronized void close() {}
}
