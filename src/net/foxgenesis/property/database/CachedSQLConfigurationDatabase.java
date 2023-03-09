package net.foxgenesis.property.database;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import net.foxgenesis.property.CachedObject;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
public class CachedSQLConfigurationDatabase extends SQLConfigurationDatabase {
	private final ConcurrentHashMap<String, CachedObject<String>> cache = new ConcurrentHashMap<>();
	private final long cacheTimeMilli;

	/**
	 * NEED_JAVADOC
	 * 
	 * @param name
	 * @param database
	 * @param table
	 * @param cacheTimeMilli
	 */
	public CachedSQLConfigurationDatabase(@Nonnull String name, @Nonnull String database, @Nonnull String table, long cacheTimeMilli) {
		super(name, database, table);
		this.cacheTimeMilli = cacheTimeMilli;
	}

	@Override
	protected boolean putInternal(@Nonnull Long lookup, @Nonnull String key, @Nonnull String value) {
		getCached(lookup, key).set(value);
		return super.putInternal(lookup, key, value);
	}

	@Override
	protected Optional<String> getInternal(@Nonnull Long lookup, @Nonnull String key) {
		return Optional.ofNullable(getCached(lookup, key).get());
	}

	@Nonnull
	private CachedObject<String> getCached(long lookup, @Nonnull String key) {
		return cache.computeIfAbsent(lookup + ":" + key,
				k -> new CachedObject<String>(() -> super.getInternal(lookup, key).orElse(null), cacheTimeMilli));
	}

	/**
	 * Get the expiration time of cached objects (in milliseconds).
	 * 
	 * @return Returns the duration of time (in milliseconds) objects are left in
	 *         the cache before getting a new value
	 */
	public long getCacheTime() { return cacheTimeMilli; }

	@Override
	public synchronized void close() {
		super.close();
		cache.clear();
	}
}
