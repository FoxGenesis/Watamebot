package net.foxgenesis.property3.database;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import net.foxgenesis.database.AbstractDatabase;
import net.foxgenesis.property3.PropertyException;
import net.foxgenesis.property3.PropertyInfo;
import net.foxgenesis.property3.impl.LCKPropertyResolver;
import net.foxgenesis.util.resource.FormattedModuleResource;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LCKConfigurationDatabase extends AbstractDatabase implements LCKPropertyResolver {
	private static final long MIN_TIMESTAMP = 1420070400000L << 22;
	private static final long MAX_TIMESTAMP = Long.parseUnsignedLong("18446744073709551615");

	public static final int MAX_CATEGORY_LENGTH = 50;
	public static final int MAX_KEY_LENGTH = 500;

	private final String database;
	private final String table;
	private final String propertyInfoTable;

	public LCKConfigurationDatabase(@NotNull String name, @NotNull String database, @NotNull String propertyTable,
			@NotNull String propertyInfoTable) {
		super(name,
				new FormattedModuleResource("watamebot", "/META-INF/configDatabase/statements.kvp",
						Map.of("database", database, "table", propertyTable, "table2", propertyInfoTable)),
				new FormattedModuleResource("watamebot", "/META-INF/configDatabase/setup.sql",
						Map.of("database", database, "table", propertyTable, "table2", propertyInfoTable)));
		this.database = Objects.requireNonNull(database);
		this.table = Objects.requireNonNull(propertyTable);
		this.propertyInfoTable = Objects.requireNonNull(propertyInfoTable);
	}

	@Override
	public boolean removeInternal(long lookup, @NotNull PropertyInfo info) throws PropertyException {
		validate(lookup, info);
		try {
			return this.mapStatement("property_delete", statement -> {
				statement.setLong(1, lookup);
				statement.setInt(2, info.id());
				return statement.executeUpdate() > 0;
			}).orElse(false);
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	public <U extends Serializable> boolean putInternal(long lookup, @NotNull PropertyInfo info, @Nullable U value)
			throws PropertyException {
		validate(lookup, info);
		try {
			return this.mapStatement("property_insert_update", statement -> {
				statement.setLong(1, lookup);
				statement.setInt(2, info.id());
				statement.setBlob(3, new SerialBlob(SerializationUtils.serialize(value)));
				return statement.executeUpdate() > 0;
			}).orElse(false);
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	public boolean putInternal(long lookup, @NotNull PropertyInfo info, @Nullable InputStream in)
			throws PropertyException {
		validate(lookup, info);
		try {
			return this.mapStatement("property_insert_update", statement -> {
				statement.setLong(1, lookup);
				statement.setInt(2, info.id());
				statement.setBlob(3, in);
				return statement.executeUpdate() > 0;
			}).orElse(false);
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	public Optional<Blob> getInternal(long lookup, @NotNull PropertyInfo info) throws PropertyException {
		validate(lookup, info);
		try {
			return this.mapStatement("property_read", statement -> {
				statement.setLong(1, lookup);
				statement.setInt(2, info.id());

				try (ResultSet result = statement.executeQuery()) {
					if (result.next())
						return result.getBlob("property");
					return null;
				}
			});
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	public boolean isPresent(long lookup, @NotNull PropertyInfo info) throws PropertyException {
		validate(lookup, info);
		try {
			return this.mapStatement("property_exists", statement -> {
				statement.setLong(1, lookup);
				statement.setInt(2, info.id());

				try (ResultSet result = statement.executeQuery()) {
					return result.next();
				}
			}).orElse(false);
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	public PropertyInfo createPropertyInfo(@NotNull String category, @NotNull String key, boolean modifiable)
			throws PropertyException {
		validate(category, key);
		try {
			return this.mapStatement("property_info_create", statement -> {
				statement.setString(1, category);
				statement.setString(2, key);
				statement.setBoolean(3, modifiable);

				if (statement.executeUpdate() > 0) {
					try (ResultSet result = statement.getGeneratedKeys()) {
						result.next();
						return result.getInt(1);
					}
				}

				return -1;
			}, Statement.RETURN_GENERATED_KEYS).filter(id -> id != -1)
					.map(id -> new PropertyInfo(id, category, key, modifiable)).orElseThrow();
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	public boolean propertyInfoExists(@NotNull String category, @NotNull String key) throws PropertyException {
		validate(category, key);
		try {
			return this.mapStatement("property_info_exists", statement -> {
				statement.setString(1, category);
				statement.setString(2, key);

				try (ResultSet result = statement.executeQuery()) {
					if (result.next())
						return result.getBoolean(1);
					return false;
				}
			}).orElse(false);
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	@NotNull
	public PropertyInfo getPropertyInfo(@NotNull String category, @NotNull String key)
			throws PropertyException, NoSuchElementException {
		validate(category, key);
		try {
			return this.mapStatement("property_info_read", statement -> {
				statement.setString(1, category);
				statement.setString(2, key);

				try (ResultSet result = statement.executeQuery()) {
					if (result.next())
						return parsePropertyInfo(result);
					return null;
				}
			}).orElseThrow();
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	@NotNull
	public List<PropertyInfo> getPropertyList() throws PropertyException {
		try {
			List<PropertyInfo> list = new ArrayList<>();
			this.prepareStatement("property_info_get_all", statement -> {
				try (ResultSet result = statement.executeQuery()) {
					while (result.next()) {
						list.add(parsePropertyInfo(result));
					}
				}
			});
			return list;
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	protected void onReady() {}

	@Override
	public void close() {}

	@NotNull
	public String getDatabase() {
		return database;
	}

	@NotNull
	public String getPropertyTable() {
		return table;
	}

	@NotNull
	public String getPropertyInfoTable() {
		return propertyInfoTable;
	}

	protected boolean isValidLookup(long lookup) {
		return lookup >= MIN_TIMESTAMP && lookup <= MAX_TIMESTAMP;
	}

	private void validate(long lookup, @Nullable PropertyInfo info) {
		// Validate lookup
		if (!isValidLookup(lookup))
			throw new PropertyException("Invalid lookup");

		// Validate fields and connection
		Objects.requireNonNull(info);
		validate(info.category(), info.name());
	}

	private void validate(String category, String key) {
		// Validate fields
		if (category.isBlank() || key.isBlank())
			throw new IllegalArgumentException("Category and key can not be blank!");
		if (category.length() > MAX_CATEGORY_LENGTH)
			throw new IllegalArgumentException("Category length can not exceed " + MAX_CATEGORY_LENGTH + " characters");
		if (key.length() > MAX_KEY_LENGTH)
			throw new IllegalArgumentException("Key length can not exceed " + MAX_KEY_LENGTH + " characters");

		// Validate database connection
		if (!isReady())
			throw new PropertyException("Database is not ready yet!");
	}

	private static PropertyInfo parsePropertyInfo(ResultSet result) throws SQLException {
		int id = result.getInt(1);
		String category = result.getString(2);
		String name = result.getString(3);
		boolean modifiable = result.getBoolean(4);
		return new PropertyInfo(id, category, name, modifiable);
	}
}
