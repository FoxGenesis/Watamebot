package net.foxgenesis.watame.property.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.property.lck.LCKPropertyResolver;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyProvider;

import org.jetbrains.annotations.NotNull;

public class PluginPropertyProviderImpl implements PluginPropertyProvider {
	private final CopyOnWriteArrayList<PluginProperty> map = new CopyOnWriteArrayList<>();
	private final LCKPropertyResolver database;
	private final long cacheTime;

	@SuppressWarnings("exports")
	public PluginPropertyProviderImpl(@NotNull LCKPropertyResolver database, long cacheTime) {
		this.database = Objects.requireNonNull(database);
		this.cacheTime = cacheTime;
	}

	@Override
	public PropertyInfo registerProperty(Plugin plugin, String key, boolean modifiable, PropertyType type) {
		if (!propertyExists(plugin, key))
			return database.createPropertyInfo(plugin.name, key, modifiable, type);
		return database.getPropertyInfo(plugin.name, key);
	}

	@Override
	public PluginProperty upsertProperty(Plugin plugin, String key, boolean modifiable, PropertyType type) {
		if (!propertyExists(plugin, key))
			registerProperty(plugin, key, modifiable, type);
		return getProperty(plugin, key);
	}

	@Override
	public PluginProperty getProperty(Plugin plugin, String key) {
		PluginProperty cached = inCache(plugin, key);
		if (cached != null)
			return cached;
		return getProperty(database.getPropertyInfo(plugin.name, key));
	}

	@Override
	public PluginProperty getProperty(PropertyInfo info) {
		PluginProperty cached = inCache(info);
		if (cached != null)
			return cached;
		cached = new CachedPluginProperty(info, database, cacheTime);
		map.addIfAbsent(cached);
		return cached;
	}

	@Override
	public boolean propertyExists(Plugin plugin, String key) {
		if (inCache(plugin, key) != null)
			return true;
		return database.isRegistered(plugin.name, key);
	}

	@SuppressWarnings("null")
	@Override
	public List<PropertyInfo> getPropertyList() {
		return map.stream().map(PluginProperty::getInfo).toList();
	}

	@Override
	public PluginProperty getPropertyByID(int id) {
		PluginProperty info = inCache(id);
		if (info != null)
			return info;
		return getProperty(database.getPropertyByID(id));
	}

	/**
	 * Check if a {@link PluginProperty} is inside the cache.
	 * 
	 * @param plugin - property owner
	 * @param key    - property name
	 * 
	 * @return Returns the found {@link PluginProperty}, otherwise {@code null}
	 */
	private PluginProperty inCache(Plugin plugin, String key) {
		for (PluginProperty pair : map)
			if (pair.getInfo().category().equalsIgnoreCase(plugin.name) && pair.getInfo().name().equalsIgnoreCase(key))
				return pair;
		return null;
	}

	/**
	 * Check if a {@link PluginProperty} is inside the cache.
	 * 
	 * @param info - property info
	 * 
	 * @return Returns the found {@link PluginProperty}, otherwise {@code null}
	 */
	private PluginProperty inCache(PropertyInfo info) {
		for (PluginProperty pair : map)
			if (pair.getInfo().equals(info))
				return pair;
		return null;
	}

	/**
	 * Check if a {@link PluginProperty} is inside the cache
	 * 
	 * @param id - property id
	 * 
	 * @return Returns the found {@link PluginProperty}, otherwise {@code null}
	 */
	private PluginProperty inCache(int id) {
		for (PluginProperty pair : map)
			if (pair.getInfo().id() == id)
				return pair;
		return null;
	}
}
