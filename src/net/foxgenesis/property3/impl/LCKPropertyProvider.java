package net.foxgenesis.property3.impl;

import java.util.List;
import java.util.Objects;

import net.foxgenesis.property3.PropertyInfo;
import net.foxgenesis.property3.PropertyProvider;

import org.jetbrains.annotations.NotNull;

public class LCKPropertyProvider implements PropertyProvider {

	private final LCKPropertyResolver database;
	
	public LCKPropertyProvider(LCKPropertyResolver database) {
		this.database = Objects.requireNonNull(database);
	}
	
	@Override
	public PropertyInfo registerProperty(@NotNull String category, @NotNull String key, boolean modifiable) {
		if(!propertyExists(category, key))
			return database.createPropertyInfo(category, key, modifiable);
		return database.getPropertyInfo(category, key);
	}

	@Override
	public LCKProperty getProperty(@NotNull String category, @NotNull String key) {
		return getProperty(database.getPropertyInfo(category, key));
	}

	@Override
	public LCKProperty getProperty(@NotNull PropertyInfo info) {
		return new LCKProperty(info, database);
	}
	
	@Override
	public boolean propertyExists(@NotNull String category, @NotNull String key) {
		return database.propertyInfoExists(category, key);
	}

	@Override
	public List<PropertyInfo> getPropertyList() {
		return database.getPropertyList();
	}
}
