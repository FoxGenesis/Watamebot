package net.foxgenesis.watame.property;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.property.impl.LCKPropertyResolver;
import net.foxgenesis.watame.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

public class PluginPropertyProviderImpl implements PluginPropertyProvider {
	private final CopyOnWriteArrayList<PluginProperty> map = new CopyOnWriteArrayList<>();
	private final LCKPropertyResolver database;
	private final long cacheTime;

	@SuppressWarnings("exports")
	public PluginPropertyProviderImpl(LCKPropertyResolver database, long cacheTime) {
		this.database = Objects.requireNonNull(database);
		this.cacheTime = cacheTime;
	}

	@Override
	public PropertyInfo registerProperty(@NotNull Plugin plugin, @NotNull String key, boolean modifiable, @NotNull PropertyType type) {
		if (!propertyExists(plugin, key)) 
			return database.createPropertyInfo(plugin.name, key, modifiable, type);
		return database.getPropertyInfo(plugin.name, key);
	}
	
	@Override
	public PluginProperty upsertProperty(@NotNull Plugin plugin, @NotNull String key, boolean modifiable, @NotNull PropertyType type) {
		if(!propertyExists(plugin, key))
			registerProperty(plugin, key, modifiable, type);
		return getProperty(plugin, key);
	}

	@Override
	public PluginProperty getProperty(@NotNull Plugin plugin, @NotNull String key) {
		PluginProperty cached = inCache(plugin, key);
		if (cached != null)
			return cached;
		return getProperty(database.getPropertyInfo(plugin.name, key));
	}

	@Override
	public PluginProperty getProperty(@NotNull PropertyInfo info) {
		PluginProperty cached = inCache(info);
		if (cached != null)
			return cached;
		cached = new CachedPluginProperty(info, database, cacheTime);
		map.addIfAbsent(cached);
		return cached;
	}

	@Override
	public boolean propertyExists(@NotNull Plugin plugin, @NotNull String key) {
		if (inCache(plugin, key) != null)
			return true;
		return database.propertyInfoExists(plugin.name, key);
	}

	@SuppressWarnings("null")
	@Override
	public List<PropertyInfo> getPropertyList() {
		return map.stream().map(PluginProperty::getInfo).toList();
	}

	@Override
	public PluginProperty getPropertyInfoByID(int id) {
		PluginProperty info = inCache(id);
		if (info != null)
			return info;
		return getProperty(database.getPropertyByID(id));
	}

	private PluginProperty inCache(Plugin plugin, String key) {
		for (PluginProperty pair : map)
			if (pair.getInfo().category().equalsIgnoreCase(plugin.name) && pair.getInfo().name().equalsIgnoreCase(key))
				return pair;
		return null;
	}

	private PluginProperty inCache(PropertyInfo info) {
		for (PluginProperty pair : map)
			if (pair.getInfo().equals(info))
				return pair;
		return null;
	}

	private PluginProperty inCache(int id) {
		for (PluginProperty pair : map)
			if (pair.getInfo().id() == id)
				return pair;
		return null;
	}
}
