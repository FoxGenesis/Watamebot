package net.foxgenesis.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.util.ResourceUtils.ModuleResource;

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
	 * Map containing key value pairs of the KVP file.
	 */
	private final HashMap<String, String> config = new HashMap<>();

	/**
	 * Create a new instance with an empty mapping.
	 * 
	 * @see #parse(InputStream)
	 * @see #parse(URL)
	 */
	public KVPFile() {}

	/**
	 * Parse a {@link File} into a KVP (Key Value Pair) file.
	 *
	 * @param file - {@link File} to parse
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 */
	public KVPFile(@Nonnull File file) throws IOException { this(file.toURI().toURL()); }

	/**
	 * Parse a {@link URL} into a KVP (Key Value Pair) file.
	 *
	 * @param url - {@link URL} to parse
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 */
	public KVPFile(@Nonnull URL url) throws IOException {
		// Ensure url is not null
		Objects.requireNonNull(url);
		parse(url);
	}

	/**
	 * Parse an {@link InputStream} into a KVP (Key Value Pair) file.
	 * 
	 * @param input - the {@link InputStream} to parse
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 */
	public KVPFile(@Nonnull InputStream input) throws IOException {
		// Ensure the stream is not null
		Objects.requireNonNull(input);
		parse(input);
	}

	/**
	 * Parse A {@link ModuleResource} into a KVP (Key Value Pair) file.
	 * 
	 * @param resource - the {@link ModuleResource} to parse
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 */
	public KVPFile(@Nonnull ModuleResource resource) throws IOException {
		// Ensure the resource is not null
		Objects.requireNonNull(resource);
		parse(resource);
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
	public void forEach(BiConsumer<? super String, ? super String> action) { config.forEach(action); }

	/**
	 * Returns the value to which the specified key is mapped, or {@code null} if
	 * this map contains no mapping for the key.
	 *
	 * <p>
	 * More formally, if this map contains a mapping from a key {@code k} to a value
	 * {@code v} such that {@code Objects.equals(key, k)}, then this method returns
	 * {@code v}; otherwise it returns {@code null}. (There can be at most one such
	 * mapping.)
	 *
	 * <p>
	 * If this map permits null values, then a return value of {@code null} does not
	 * <i>necessarily</i> indicate that the map contains no mapping for the key;
	 * it's also possible that the map explicitly maps the key to {@code null}. The
	 * {@link #containsKey containsKey} operation may be used to distinguish these
	 * two cases.
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or {@code null} if
	 *         this map contains no mapping for the key
	 * @throws ClassCastException   if the key is of an inappropriate type for this
	 *                              map (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified key is null and this map does
	 *                              not permit null keys (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @see #getOrDefault(String, String)
	 */
	@Nullable
	public String get(String key) { return config.get(key); }

	/**
	 * Returns the value to which the specified key is mapped, or
	 * {@code defaultValue} if this map contains no mapping for the key.
	 *
	 * @param key          the key whose associated value is to be returned
	 * @param defaultValue the default mapping of the key
	 * @return the value to which the specified key is mapped, or
	 *         {@code defaultValue} if this map contains no mapping for the key
	 * @throws ClassCastException   if the key is of an inappropriate type for this
	 *                              map (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified key is null and this map does
	 *                              not permit null keys (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @see #get(String)
	 */
	@Nullable
	public String getOrDefault(String key, String defaultValue) { return config.getOrDefault(key, defaultValue); }

	/**
	 * Returns {@code true} if this map contains a mapping for the specified key.
	 * More formally, returns {@code true} if and only if this map contains a
	 * mapping for a key {@code k} such that {@code Objects.equals(key, k)}. (There
	 * can be at most one such mapping.)
	 *
	 * @param key key whose presence in this map is to be tested
	 * @return {@code true} if this map contains a mapping for the specified key
	 * @throws ClassCastException   if the key is of an inappropriate type for this
	 *                              map (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if the specified key is null and this map does
	 *                              not permit null keys (<a href=
	 *                              "{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	public boolean containsKey(String key) { return config.containsKey(key); }

	/**
	 * Clear the configuration mapping.
	 */
	public void clear() { config.clear(); }

	/**
	 * Checks if the configuration mapping is empty.
	 *
	 * @return Returns {@code true} if there are no configuration entries mapped
	 */
	public boolean isEmpty() { return config.isEmpty(); }

	/**
	 * Parse a resource {@link URL} into the configuration mapping.
	 *
	 * @param resourceURL - URL pointing to the resource to parse
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 */
	public void parse(@Nonnull URL resourceURL) throws IOException {
		parse(ResourceUtils.linesFromResource(Objects.requireNonNull(resourceURL, "Resource url must not be null!")));
	}

	/**
	 * Parse an {@link InputStream} into the configuration mapping.
	 * 
	 * @param input - the input stream to parse
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 */
	public void parse(@Nonnull InputStream input) throws IOException {
		parse(List.of(ResourceUtils.toSplitString(Objects.requireNonNull(input, "InputStream must not be null!"))));
	}

	/**
	 * Parse a {@link ModuleResource} into the configuration mapping.
	 * 
	 * @param resource - the resource to parse
	 * @throws IOException Thrown if an error occurs while reading the InputStream
	 *                     of the resource
	 */
	public void parse(@Nonnull ModuleResource resource) throws IOException { parse(resource.openStream()); }

	/**
	 * Convert the parsed inputs into a key value mapping
	 * 
	 * @param input - raw KVP style strings
	 */
	private void parse(List<String> input) {
		if(input == null)
			return;
		/*
		 * - Read all lines - Filter out ignored lines - Split line based on regex with
		 * limit of two - Ensure split has two elements - Collect into a map
		 */
		Map<String, String> tempMap = input.stream().filter(ignoreLines).map(line -> line.split("=", 2))
				.filter(split -> split.length == 2)
				.collect(Collectors.toMap(split -> split[0].trim(), split -> split[1].trim()));

		// Put all pairs into main map
		config.putAll(tempMap);
	}
}
