package net.foxgenesis.property.lck.impl;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.lck.LCKPropertyResolver;

public class CachedLCKProperty extends LCKPropertyImpl {
	protected final ConcurrentHashMap<Long, CachedObject<Optional<BlobMapping>>> cache = new ConcurrentHashMap<>();
	private final long cacheTime;

	public CachedLCKProperty(PropertyInfo info, LCKPropertyResolver resolver, long cacheTime) {
		super(info, resolver);
		this.cacheTime = cacheTime;
	}

	@Override
	public Optional<BlobMapping> get(Long lookup) {
		init(lookup);
		Optional<BlobMapping> map = cache.get(lookup).get();
		if (map != null)
			return map;
		return Optional.empty();
	}

	@Override
	public boolean set(Long lookup, byte[] data, boolean isUserInput) {
		checkUserInput(isUserInput);
		init(lookup);
		if (super.set(lookup, data, isUserInput)) {
			cache.get(lookup).set(Optional.ofNullable(createMapping(lookup, data)));
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
		return new CachedObject<>(() -> retrieve(lookup), cacheTime);
	}

	private Optional<BlobMapping> retrieve(Long lookup) {
		return resolver.getInternal(lookup, getInfo()).map(b -> createMapping(lookup, b));
	}

	protected BlobMapping createMapping(Long lookup, Blob blob) throws PropertyException {
		if (blob == null)
			return null;

		try {
			return new BlobMapping(lookup, blob, getInfo().type());
		} catch (IOException | SQLException e) {
			throw new PropertyException(e);
		}
	}

	protected BlobMapping createMapping(Long lookup, byte[] data) {
		if (data == null || data.length == 0)
			return null;
		return new BlobMapping(lookup, data, getInfo().type());
	}
}
