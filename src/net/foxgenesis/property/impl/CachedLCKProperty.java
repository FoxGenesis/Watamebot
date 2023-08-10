package net.foxgenesis.property.impl;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyInfo;

public class CachedLCKProperty extends LCKProperty {
	protected final ConcurrentHashMap<Long, CachedObject<Optional<BlobMapping>>> cache = new ConcurrentHashMap<>();
	private final long cacheTime;

	public CachedLCKProperty(PropertyInfo info, LCKPropertyResolver resolver, long cacheTime) {
		super(info, resolver);
		this.cacheTime = cacheTime;
	}

	@Override
	public Optional<BlobMapping> get(long lookup) {
		init(lookup);
		Optional<BlobMapping> map = cache.get(lookup).get();
		if (map != null)
			return map;
		return Optional.empty();
	}

	@Override
	public boolean set(long lookup, byte[] data) {
		init(lookup);
		if (super.set(lookup, data)) {
			cache.get(lookup).update(Optional.ofNullable(createMapping(lookup, data)));
			return true;
		}
		return false;
	}

	public long getCacheTime() {
		return cacheTime;
	}

	protected void init(long lookup) {
		if (!cache.containsKey(lookup))
			cache.put(lookup, newCache(lookup));
	}
	
	private CachedObject<Optional<BlobMapping>> newCache(long lookup) {
		return new CachedObject<Optional<BlobMapping>>(() -> retrieve(lookup), cacheTime);
	}
	
	private Optional<BlobMapping> retrieve(long lookup) {
		return resolver.getInternal(lookup, getInfo()).map(b -> createMapping(lookup, b));
	}

	protected BlobMapping createMapping(long lookup, Blob blob) throws PropertyException {
		if (blob == null)
			return null;

		try {
			return new BlobMapping(lookup, blob, getInfo().type());
		} catch (IOException | SQLException e) {
			throw new PropertyException(e);
		}
	}

	protected BlobMapping createMapping(long lookup, byte[] data) {
		if (data == null || data.length == 0)
			return null;
		BlobMapping map = new BlobMapping(lookup, data, getInfo().type());
		return map;
	}
}
