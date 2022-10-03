package net.foxgenesis.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.foxgenesis.util.ResourceHelper;

/**
 * Key Value Pair (KVP) file implementation.
 * 
 * @author Ashley
 *
 */
public class KVPFile {

	/**
	 * Predicate to check if a line should be parsed or not.
	 */
	private static final Predicate<String> ignoreLines = line -> line.matches("^(.+?)=(.*?)$");

	/**
	 * File refrence for this KVP File.
	 */
	private final URL resourceURL;

	/**
	 * Map containing key value pairs of the KVP file.
	 */
	private final HashMap<String, String> config = new HashMap<>();

	/**
	 * Parse a {@link File} into a KVP (Key Value Pair) file.
	 * 
	 * @param file - {@link File} to parse
	 * @throws MalformedURLException
	 */
	public KVPFile(@Nonnull File file) throws MalformedURLException {
		this(file.toURI().toURL());
	}

	/**
	 * Parse a {@link URL} into a KVP (Key Value Pair) file.
	 * 
	 * @param url - {@link URL} to parse
	 */
	public KVPFile(@Nonnull URL url) {
		// Ensure url is not null
		Objects.nonNull(url);
		this.resourceURL = url;
	}

	/**
	 * Performs the given action for each entry in this map until all entrieshave
	 * been processed or the action throws an exception. Unlessotherwise specified
	 * by the implementing class, actions are performed inthe order of entry set
	 * iteration (if an iteration order is specified.)Exceptions thrown by the
	 * action are relayed to the caller.
	 * 
	 * @param action - The action to be performed for each entry
	 * @see Map#forEach(BiConsumer)
	 */
	public void forEach(BiConsumer<? super String, ? super String> action) {
		config.forEach(action);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param key
	 * @return
	 * @see HashMap#get(Object)
	 */
	@Nullable
	public String get(String key) {
		return config.get(key);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param key
	 * @return
	 * @see HashMap#getOrDefault(Object, Object)
	 */
	@Nullable
	public String getOrDefault(String key, String defaultValue) {
		return config.getOrDefault(key, defaultValue);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param key
	 * @return
	 * @see HashMap#containsKey(Object)
	 */
	public boolean containsKey(String key) {
		return config.containsKey(key);
	}

	/**
	 * Clear the configuration mapping.
	 */
	public void clear() {
		config.clear();
	}

	/**
	 * Checks if the configuration mapping is empty.
	 * 
	 * @return Returns {@code true} if there are no configuration entries mapped
	 */
	public boolean isEmpty() {
		return config.isEmpty();
	}

	/**
	 * Parse the resource {@link URL} into the configuration mapping.
	 * 
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 */
	public void parse() throws IOException {
		/*
		 * - Read all lines - Filter out ignored lines - Split line based on regex with
		 * limit of two - Ensure split has two elements - Collect into a map
		 */
		Map<String, String> tempMap = ResourceHelper.linesFromResource(resourceURL).stream().filter(ignoreLines)
				.map(line -> line.split("=", 2)).filter(split -> split.length == 2)
				.collect(Collectors.toMap(split -> split[0], split -> split[1]));

		// Put all pairs into main map
		config.putAll(tempMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(config, resourceURL);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KVPFile other = (KVPFile) obj;
		return Objects.equals(config, other.config) && Objects.equals(resourceURL, other.resourceURL);
	}

	@Override
	public String toString() {
		return "KVPFile [resourceURL=" + resourceURL + ", config=" + config + "]";
	}
}
