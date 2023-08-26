package net.foxgenesis.watame.property.impl;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.property.lck.LCKPropertyResolver;
import net.foxgenesis.property.lck.impl.CachedObject;
import net.foxgenesis.watame.property.PluginPropertyMapping;

import net.dv8tion.jda.api.entities.Guild;

public class CachedPluginProperty extends PluginPropertyImpl {
	protected final ConcurrentHashMap<Long, CachedObject<PluginPropertyMapping>> cache = new ConcurrentHashMap<>();
	private final long cacheTime;

	@SuppressWarnings("exports")
	public CachedPluginProperty(PropertyInfo info, LCKPropertyResolver resolver, long cacheTime) {
		super(info, resolver);
		this.cacheTime = cacheTime;
	}

	@Override
	public Optional<PluginPropertyMapping> get(Guild lookup) {
		init(lookup);
		return Optional.ofNullable(cache.get(lookup.getIdLong()).get());
	}

	@Override
	public boolean set(Guild lookup, byte[] data, boolean isUserInput) {
		checkUserInput(isUserInput);
		init(lookup);
		if (super.set(lookup, data, isUserInput)) {
			cache.get(lookup.getIdLong()).set(createMapping(lookup, data, getInfo().type()));
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Guild lookup, boolean isUserInput) {
		checkUserInput(isUserInput);
		if (super.remove(lookup, isUserInput)) {
			cache.get(lookup.getIdLong()).set(null);
			return true;
		}
		return false;
	}

	protected void init(Guild lookup) {
		if (!cache.containsKey(lookup.getIdLong()))
			cache.put(lookup.getIdLong(), new CachedObject<>(() -> super.get(lookup).orElse(null), cacheTime));
	}

	protected PluginPropertyMapping createMapping(Guild lookup, byte[] data, PropertyType type) {
		if (data == null || data.length == 0)
			return null;
		return new PluginPropertyMapping(lookup, data, type);
	}
}