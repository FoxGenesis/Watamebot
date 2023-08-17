package net.foxgenesis.property.lck.impl;

import java.util.List;
import java.util.Objects;

import net.foxgenesis.property.Property;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyMapping;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.property.lck.LCKPropertyProvider;
import net.foxgenesis.property.lck.LCKPropertyResolver;

public class LCKPropertyProviderImpl implements LCKPropertyProvider {

	private final LCKPropertyResolver database;

	public LCKPropertyProviderImpl(LCKPropertyResolver database) {
		this.database = Objects.requireNonNull(database);
	}

	@Override
	public PropertyInfo registerProperty(String category, String key, boolean modifiable, PropertyType type) {
		if (!propertyExists(category, key))
			return database.createPropertyInfo(category, key, modifiable, type);
		return database.getPropertyInfo(category, key);
	}

	@Override
	public Property<Long, PropertyMapping> getPropertyByID(int id) {
		return getProperty(database.getPropertyByID(id));
	}

	@Override
	public LCKPropertyImpl getProperty(String category, String key) {
		return getProperty(database.getPropertyInfo(category, key));
	}

	@Override
	public LCKPropertyImpl getProperty(PropertyInfo info) {
		return new LCKPropertyImpl(info, database);
	}

	@Override
	public boolean propertyExists(String category, String key) {
		return database.isRegistered(category, key);
	}

	@Override
	public List<PropertyInfo> getPropertyList() {
		return database.getPropertyList();
	}
}
