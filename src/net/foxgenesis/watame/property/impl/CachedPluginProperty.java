package net.foxgenesis.watame.property.impl;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.foxgenesis.property.PropertyException;
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
	public boolean set(Guild lookup, byte[] data) {
		init(lookup);
		if (super.set(lookup, data)) {
			cache.get(lookup.getIdLong()).update(createMapping(lookup, data, getInfo().type()));
			return true;
		}
		return false;
	}

	protected void init(Guild lookup) {
		if (!cache.containsKey(lookup.getIdLong()))
			cache.put(lookup.getIdLong(), new CachedObject<>(() -> retrieve(lookup), cacheTime));
	}

	private PluginPropertyMapping retrieve(Guild lookup) {
		return resolver.getInternal(lookup.getIdLong(), getInfo()).map(b -> createMapping(lookup, b, getInfo().type()))
				.orElse(null);
	}

	protected PluginPropertyMapping createMapping(Guild lookup, Blob blob, PropertyType type) throws PropertyException {
		if (blob == null)
			return null;

		try {
			return new PluginPropertyMapping(lookup, blob, type);
		} catch (IOException | SQLException e) {
			throw new PropertyException(e);
		}
	}

	protected PluginPropertyMapping createMapping(Guild lookup, byte[] data, PropertyType type) {
		if (data == null || data.length == 0)
			return null;
		return new PluginPropertyMapping(lookup, data, type);
	}
}