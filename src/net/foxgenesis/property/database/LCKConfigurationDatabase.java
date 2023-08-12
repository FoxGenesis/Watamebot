package net.foxgenesis.property.database;

import java.io.InputStream;
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

import net.foxgenesis.database.AbstractDatabase;
import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.property.lck.LCKPropertyResolver;
import net.foxgenesis.util.resource.FormattedModuleResource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LCKConfigurationDatabase extends AbstractDatabase implements LCKPropertyResolver {
	public static final int MAX_CATEGORY_LENGTH = 50;
	public static final int MAX_KEY_LENGTH = 500;

	private final String database;
	private final String table;
	private final String propertyInfoTable;

	public LCKConfigurationDatabase(String database, String propertyTable, String propertyInfoTable) {
		super("LCK Configuration",
				new FormattedModuleResource("watamebot", "/META-INF/configDatabase/statements.kvp",
						Map.of("database", database, "table", propertyTable, "table2", propertyInfoTable)),
				new FormattedModuleResource("watamebot", "/META-INF/configDatabase/setup.sql",
						Map.of("database", database, "table", propertyTable, "table2", propertyInfoTable)));
		this.database = Objects.requireNonNull(database);
		table = Objects.requireNonNull(propertyTable);
		this.propertyInfoTable = Objects.requireNonNull(propertyInfoTable);
	}

	@Override
	public boolean removeInternal(Long lookup, PropertyInfo info) throws PropertyException {
		validate(lookup, info);
		logger.debug("Removing property: {}", info);
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
	public boolean putInternal(Long lookup, PropertyInfo info, InputStream in) throws PropertyException {
		try (in) {
			validate(lookup, info);
			if (in == null)
				return removeInternal(lookup, info);

			logger.debug("Setting property: {}", info);
			return this.mapStatement("property_insert_update", statement -> {
				statement.setLong(1, lookup);
				statement.setInt(2, info.id());
				statement.setBlob(3, in);
				return statement.executeUpdate() > 0;
			}).orElse(false);
		} catch (Exception e) {
			throw new PropertyException(e);
		}
	}

	@Override
	public Optional<Blob> getInternal(Long lookup, PropertyInfo info) throws PropertyException {
		validate(lookup, info);
		logger.debug("Getting property: {}", info);
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
	public boolean isPresent(Long lookup, PropertyInfo info) throws PropertyException {
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
	public PropertyInfo createPropertyInfo(String category, String key, boolean modifiable, PropertyType type)
			throws PropertyException, IllegalArgumentException {
		validate(category, key);
		logger.debug("Creating property: [{}] {} (modifiable: {}, type: {})", category, key, modifiable, type);
		try {
			return this.mapStatement("property_info_create", statement -> {
				statement.setString(1, category);
				statement.setString(2, key);
				statement.setBoolean(3, modifiable);
				statement.setString(4, type.name().toLowerCase());

				if (statement.executeUpdate() > 0) {
					try (ResultSet result = statement.getGeneratedKeys()) {
						result.next();
						return result.getInt(1);
					}
				}

				return -1;
			}, Statement.RETURN_GENERATED_KEYS).filter(id -> id != -1)
					.map(id -> new PropertyInfo(id, category, key, modifiable, type))
					.orElseThrow(() -> new IllegalArgumentException("Property already exists"));
		} catch (SQLException e) {
			throw new PropertyException(e);
		}
	}

	@Override
	public PropertyInfo getPropertyByID(int id) throws PropertyException, NoSuchElementException {
		if (id < 0)
			throw new PropertyException("Invalid property id");
		// Validate database connection
		if (!isReady())
			throw new PropertyException("Database is not ready yet!");

		logger.debug("Getting property for id: {}", id);
		try {
			return this.mapStatement("property_info_read_by_id", statement -> {
				statement.setInt(1, id);

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
	public boolean isRegistered(String category, String key) throws PropertyException {
		validate(category, key);
		logger.debug("Checking for existing property [{}] {}", category, key);
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
	public PropertyInfo getPropertyInfo(String category, String key) throws PropertyException, NoSuchElementException {
		validate(category, key);
		logger.debug("Getting property info for [{}] {}", category, key);
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
	public List<PropertyInfo> getPropertyList() throws PropertyException {
		logger.debug("Getting property list");
		try {
			List<PropertyInfo> list = new ArrayList<>();
			prepareStatement("property_info_get_all", statement -> {
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

	@Override
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
		return Math.floor(Math.log10(lookup)) + 1 == 18;
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
		PropertyType type = PropertyType.valueOf(result.getString(5).toUpperCase());
		return new PropertyInfo(id, category, name, modifiable, type);
	}
}
