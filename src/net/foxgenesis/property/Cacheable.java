package net.foxgenesis.property;

/**
 * 
 * @author Ashley
 *
 */
public interface Cacheable {

	/**
	 * Check if this property can be cached.
	 * 
	 * @return Returns {@code true} if this property can be stored and expire after
	 *         a period of time
	 * @see #getCacheTime()
	 */
	boolean isCacheable();

	/**
	 * Get the cache expiration time.
	 * 
	 * @return Returns the maximum period of time (in milliseconds) this property
	 *         can be stored before it value expires.
	 * @see #canCache()
	 */
	long getExpirationTime();
}
