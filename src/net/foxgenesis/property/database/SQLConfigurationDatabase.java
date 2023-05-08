package net.foxgenesis.property.database;

import java.sql.ResultSet;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

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
	public SQLConfigurationDatabase(String name, String database, String table) { super(name, database, table); }

	@Override
	protected Optional<String> getInternal(@Nonnull Long lookup, @Nonnull String key) {
		if (!isReady())
			throw new UnsupportedOperationException("Database is not ready yet!");

		Objects.requireNonNull(lookup);
		Objects.requireNonNull(key);

		return this.mapStatement("property_get", statement -> {
			statement.setLong(1, lookup);
			statement.setString(2, key);

			try (ResultSet result = statement.executeQuery()) {
				if (result.next()) { return Optional.of(result.getString("property")); }
				return Optional.empty();
			}
		}, err -> logger.error("Error while getting internal property", err));
	}

	@Override
	protected boolean putInternal(@Nonnull Long lookup, @Nonnull String key, @Nonnull String value) {
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
		}, err -> logger.error("Error while putting internal property", err));
	}

	@Override
	protected boolean removeInternal(@Nonnull Long lookup, @Nonnull String key) {
		if (!isReady())
			throw new UnsupportedOperationException("Database is not ready yet!");

		Objects.requireNonNull(key);

		return this.mapStatement("property_delete", statement -> {
			statement.setLong(1, lookup);
			statement.setString(2, key);
			return statement.executeUpdate() > 0;
		}, err -> logger.error("Error while removing internal property", err));
	}

	@Override
	protected void onReady() {}

	@Override
	public synchronized void close() {}
}