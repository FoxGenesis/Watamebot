package net.foxgenesis.watame.plugin;

import java.util.Properties;
import java.util.function.BiConsumer;

/**
 * Wrapper class of {@link Properties} to contain read only operations.
 * 
 * @author Ashley
 *
 */
public class PluginProperties {

	/**
	 * Properties file to wrap
	 */
	private final Properties map;

	/**
	 * Wrap a {@link Properties}.
	 * 
	 * @param properties - properties object to wrap
	 */
	public PluginProperties(Properties properties) {
		this.map = properties;
	}

	/**
	 * @see Properties#getProperty(String)
	 */
	public String getProperty(String key) {
		return map.getProperty(key);
	}

	/**
	 * @see Properties#getProperty(String, String)
	 */
	public String getProperty(String key, String defaultValue) {
		return map.getProperty(key, defaultValue);
	}

	/**
	 * @see Properties#size()
	 */
	public int size() {
		return map.size();
	}

	/**
	 * @see Properties#isEmpty()
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @see Properties#contains(Object)
	 */
	public boolean contains(Object value) {
		return map.contains(value);
	}

	/**
	 * @see Properties#containsValue(Object)
	 */
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	/**
	 * @see Properties#containsKey(Object)
	 */
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/**
	 * @see Properties#get(Object)
	 */
	public Object get(Object key) {
		return map.get(key);
	}

	@Override
	/**
	 * @see Properties#toString()
	 */
	public synchronized String toString() {
		return map.toString();
	}

	/**
	 * @see Properties#getOrDefault(Object, Object)
	 */
	public Object getOrDefault(Object key, Object defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	/**
	 * @see Properties#forEach(BiConsumer)
	 */
	public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
		map.forEach(action);
	}
}
