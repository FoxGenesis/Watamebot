package net.foxgenesis.property.database;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

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
	public CachedSQLConfigurationDatabase(@NotNull String name, @NotNull String database, @NotNull String table, long cacheTimeMilli) {
		super(name, database, table);
		this.cacheTimeMilli = cacheTimeMilli;
	}

	@Override
	protected boolean putInternal(@NotNull Long lookup, @NotNull String key, @NotNull String value) {
		getCached(lookup, key).set(value);
		return super.putInternal(lookup, key, value);
	}

	@Override
	protected Optional<String> getInternal(@NotNull Long lookup, @NotNull String key) {
		return Optional.ofNullable(getCached(lookup, key).get());
	}

	@NotNull
	private CachedObject<String> getCached(long lookup, @NotNull String key) {
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
