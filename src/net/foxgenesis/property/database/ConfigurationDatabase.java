package net.foxgenesis.property.database;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import net.foxgenesis.database.AbstractDatabase;
import net.foxgenesis.property2.PropertyType;
import net.foxgenesis.property2.async.AsyncPropertyResolver;
import net.foxgenesis.util.resource.FormattedModuleResource;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
public abstract class ConfigurationDatabase<L> extends AbstractDatabase implements AsyncPropertyResolver<L> {
	/**
	 * Default executor service
	 */
	private static final ExecutorService COMMON_POOL = ForkJoinPool.commonPool();

	/**
	 * Instance default executor service
	 */
	private final ExecutorService defaultExecutor;

	/**
	 * Database name
	 */
	private final String database;

	/**
	 * Table name
	 */
	private final String table;

	/**
	 * NEED_JAVADOC
	 * 
	 * @param name
	 * @param database
	 * @param table
	 * @param service
	 */
	public ConfigurationDatabase(@Nonnull String name, @Nonnull String database, @Nonnull String table,
			@Nullable ExecutorService service) {
		super(name,
				new FormattedModuleResource("watamebot", "/META-INF/configDatabase/statements.kvp",
						Map.of("database", database, "table", table)),
				new FormattedModuleResource("watamebot", "/META-INF/configDatabase/setup.sql",
						Map.of("database", database, "table", table)));
		this.database = Objects.requireNonNull(database);
		this.table = Objects.requireNonNull(table);
		this.defaultExecutor = Objects.requireNonNullElse(service, COMMON_POOL);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param name
	 * @param database
	 * @param table
	 */
	public ConfigurationDatabase(@Nonnull String name, @Nonnull String database, @Nonnull String table) {
		this(name, database, table, null);
	}

	// ======================================= ASYNC ===============================

	/**
	 * Remove an internal value from the database asynchronously.
	 * 
	 * @param lookup  - property lookup
	 * @param key     - property key
	 * @param service - executor service
	 * @return Returns a future that, upon completion, will return {@code true} if
	 *         the property was successfully removed from the database
	 * @see #putInternalAsync(Object, String, String, ExecutorService)
	 * @see #getInternalAsync(Object, String, ExecutorService)
	 */
	@Nonnull
	protected CompletableFuture<Boolean> removeInternalAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return CompletableFuture.supplyAsync(() -> removeInternal(lookup, key), screenExecutor(service));
	}

	/**
	 * Put/Update an internal property inside the database asynchronously.
	 * 
	 * @param lookup  - property lookup
	 * @param key     - property key
	 * @param value   - value to insert
	 * @param service - executor service
	 * @return Returns a future that, upon completion, will return {@code true} if
	 *         the property was successfully added/updated in the database
	 * @see #removeInternalAsync(Object, String, ExecutorService)
	 * @see #getInternalAsync(Object, String, ExecutorService)
	 */
	@Nonnull
	protected CompletableFuture<Boolean> putInternalAsync(@Nonnull L lookup, @Nonnull String key, @Nonnull String value,
			@Nullable ExecutorService service) {
		return CompletableFuture.supplyAsync(() -> putInternal(lookup, key, value), screenExecutor(service));
	}

	/**
	 * This method contains the same execution as
	 * {@link #putInternalAsync(Object, String, String, ExecutorService)} however,
	 * the {@code value} is provided with a {@link Supplier} that is called during
	 * async processing.
	 * 
	 * @param lookup  - property lookup
	 * @param key     - property key
	 * @param value   - value supplier
	 * @param service - executor service
	 * @return Returns the same as
	 *         {@link #putInternalAsync(Object, String, String, ExecutorService)}
	 * @see #putInternalAsync(Object, String, String, ExecutorService)
	 */
	@Nonnull
	private CompletableFuture<Boolean> putInternalAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull Supplier<String> value, @Nullable ExecutorService service) {
		return CompletableFuture.supplyAsync(value, screenExecutor(service))
				.thenCompose(v -> putInternalAsync(lookup, key, v, service));
	}

	/**
	 * Get an internal property inside the database asynchronously.
	 * 
	 * @param lookup  - property lookup
	 * @param key     - property key
	 * @param service - executor service
	 * @return Returns a future {@link Optional} containing raw value data
	 * @see #removeInternalAsync(Object, String, ExecutorService)
	 * @see #putInternalAsync(Object, String, String, ExecutorService)
	 */
	@Nonnull
	protected CompletableFuture<Optional<String>> getInternalAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return CompletableFuture.supplyAsync(() -> getInternal(lookup, key), screenExecutor(service));
	}

	/**
	 * Put an array inside the database asynchronously.
	 * 
	 * @param <T>       Array type
	 * @param lookup    - property lookup
	 * @param key       - property key
	 * @param delimeter - delimiter used to separate values in the array
	 * @param arr       - array to insert
	 * @param service   - executor service
	 * @return Returns {@code true} if array was successfully inserted inside the
	 *         database
	 * @see #getArrayInternal(Long, String, String, Function, IntFunction)
	 */
	@Nonnull
	protected <T> CompletableFuture<Boolean> putArrayInternalAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull T[] arr, @Nullable ExecutorService service) {
		if (arr.length == 0)
			return CompletableFuture.failedFuture(new IllegalArgumentException("Array must not be empty!"));

		return putInternalAsync(lookup, key, () -> Arrays.stream(arr).map(Object::toString)
				.reduce((a, b) -> a.toString() + delimeter + b.toString()).get(), service);
	}

	/**
	 * Get an array from the database.
	 * 
	 * @param <T>     Array type
	 * @param lookup  - property lookup
	 * @param key     - property key
	 * @param regex   - delimiter used to separate values
	 * @param map     - function to map array entries to array data type
	 * @param arr     - function used to generate a new array
	 * @param service - executor service
	 * @return Returns an {@link Optional} containing a <b>nullable</b> array of
	 *         values
	 * @see #putArrayInternal(Long, String, String, Object[])
	 */
	@Nonnull
	protected <T> CompletableFuture<Optional<T[]>> getArrayInternalAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nonnull Function<String, T> map, @Nonnull IntFunction<T[]> arr,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApplyAsync(
				o -> o.map(str -> Arrays.stream(str.split(regex)).map(map).toArray(arr)), screenExecutor(service));
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<String>> getStringAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Boolean>> getBooleanAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Boolean::parseBoolean));
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Integer>> getIntAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Integer::parseInt));
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Float>> getFloatAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Float::parseFloat));
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Double>> getDoubleAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Double::parseDouble));
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Long>> getLongAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Long::parseLong));
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<String[]>> getStringArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Function.identity(), String[]::new, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Boolean::parseBoolean, Boolean[]::new, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Integer::parseInt, Integer[]::new, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Float::parseFloat, Float[]::new, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Double::parseDouble, Double[]::new, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Long[]>> getLongArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Long::parseLong, Long[]::new, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putStringAsync(@Nonnull L lookup, @Nonnull String key, @Nonnull String value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, value, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putBooleanAsync(@Nonnull L lookup, @Nonnull String key, boolean value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putIntAsync(@Nonnull L lookup, @Nonnull String key, int value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putFloatAsync(@Nonnull L lookup, @Nonnull String key, float value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putDoubleAsync(@Nonnull L lookup, @Nonnull String key, double value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putLongAsync(@Nonnull L lookup, @Nonnull String key, long value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putStringArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull String[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, arr, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putBooleanArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull boolean[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putIntArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull int[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putFloatArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull float[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putDoubleArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull double[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putLongArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull long[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> removeAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service) {
		return removeInternalAsync(lookup, key, service);
	}

	@Override
	@Nonnull
	public CompletableFuture<PropertyType> typeOfAsync(L lookup, String key, ExecutorService service) {
		return CompletableFuture.completedFuture(PropertyType.STRING);
	}

	// ======================================= SYNC ================================

	/**
	 * Remove an internal value from the database.
	 * 
	 * @param lookup - property lookup
	 * @param key    - property key
	 * @return Returns {@code true} if the property was successfully removed from
	 *         the database
	 * @see #putInternal(long, String, String)
	 * @see #getInternal(long, String)
	 */
	protected abstract boolean removeInternal(@Nonnull L lookup, @Nonnull String key);

	/**
	 * Put/Update an internal property inside the database.
	 * 
	 * @param lookup - property lookup
	 * @param key    - property key
	 * @param value  - value to insert
	 * @return Returns {@code true} if the property was successfully added/updated
	 *         in the database
	 * @see #removeInternal(long, String)
	 * @see #getInternal(long, String)
	 */
	protected abstract boolean putInternal(@Nonnull L lookup, @Nonnull String key, @Nonnull String value);

	/**
	 * Get an internal property inside the database.
	 * 
	 * @param lookup - property lookup
	 * @param key    - property key
	 * @return Returns an {@link Optional} containing raw value data
	 * @see #removeInternal(long, String)
	 * @see #putInternal(long, String, String)
	 */
	@Nonnull
	protected abstract Optional<String> getInternal(@Nonnull L lookup, @Nonnull String key);

	/**
	 * Put an array inside the database.
	 * 
	 * @param <T>       Array type
	 * @param lookup    - property lookup
	 * @param key       - property key
	 * @param delimeter - delimiter used to separate values in the array
	 * @param arr       - array to insert
	 * @return Returns {@code true} if array was successfully inserted inside the
	 *         database
	 * @see #getArrayInternal(Long, String, String, Function, IntFunction)
	 */
	protected <T> boolean putArrayInternal(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull T[] arr) {
		if (arr.length == 0)
			throw new IllegalArgumentException("Array must not be empty!");

		return putInternal(lookup, key, Arrays.stream(arr).map(Object::toString)
				.reduce((a, b) -> a.toString() + delimeter + b.toString()).get());
	}

	/**
	 * Get an array from the database.
	 * 
	 * @param <T>    Array type
	 * @param lookup - property lookup
	 * @param key    - property key
	 * @param regex  - delimiter used to separate values
	 * @param map    - function to map array entries to array data type
	 * @param arr    - function used to generate a new array
	 * @return Returns an {@link Optional} containing a <b>nullable</b> array of
	 *         values
	 * @see #putArrayInternal(Long, String, String, Object[])
	 */
	@Nonnull
	protected <T> Optional<T[]> getArrayInternal(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex,
			@Nonnull Function<String, T> map, @Nonnull IntFunction<T[]> arr) {
		return getInternal(lookup, key).map(str -> Arrays.stream(str.split(regex)).map(map).toArray(arr));
	}

	@Override
	@Nonnull
	public Optional<String> getString(L lookup, String key) { return getInternal(lookup, key); }

	@Override
	@Nonnull
	public Optional<Boolean> getBoolean(L lookup, String key) {
		return getInternal(lookup, key).map(Boolean::parseBoolean);
	}

	@Override
	@Nonnull
	public Optional<Integer> getInt(@Nonnull L lookup, @Nonnull String key) {
		return getInternal(lookup, key).map(Integer::parseInt);
	}

	@Override
	@Nonnull
	public Optional<Float> getFloat(@Nonnull L lookup, @Nonnull String key) {
		return getInternal(lookup, key).map(Float::parseFloat);
	}

	@Override
	@Nonnull
	public Optional<Double> getDouble(@Nonnull L lookup, @Nonnull String key) {
		return getInternal(lookup, key).map(Double::parseDouble);
	}

	@Override
	@Nonnull
	public Optional<Long> getLong(@Nonnull L lookup, @Nonnull String key) {
		return getInternal(lookup, key).map(Long::parseLong);
	}

	@Override
	@Nonnull
	public Optional<String[]> getStringArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex) {
		return getArrayInternal(lookup, key, regex, Function.identity(), String[]::new);
	}

	@Override
	@Nonnull
	public Optional<Boolean[]> getBooleanArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex) {
		return getArrayInternal(lookup, key, regex, Boolean::parseBoolean, Boolean[]::new);
	}

	@Override
	@Nonnull
	public Optional<Integer[]> getIntArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex) {
		return getArrayInternal(lookup, key, regex, Integer::parseInt, Integer[]::new);
	}

	@Override
	@Nonnull
	public Optional<Float[]> getFloatArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex) {
		return getArrayInternal(lookup, key, regex, Float::parseFloat, Float[]::new);
	}

	@Override
	@Nonnull
	public Optional<Double[]> getDoubleArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex) {
		return getArrayInternal(lookup, key, regex, Double::parseDouble, Double[]::new);
	}

	@Override
	@Nonnull
	public Optional<Long[]> getLongArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex) {
		return getArrayInternal(lookup, key, regex, Long::parseLong, Long[]::new);
	}

	@Override
	public boolean putString(@Nonnull L lookup, @Nonnull String key, @Nonnull String value) {
		return putInternal(lookup, key, value);
	}

	@Override
	public boolean putBoolean(@Nonnull L lookup, @Nonnull String key, boolean value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putInt(@Nonnull L lookup, @Nonnull String key, int value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putFloat(@Nonnull L lookup, @Nonnull String key, float value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putDouble(@Nonnull L lookup, @Nonnull String key, double value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putLong(@Nonnull L lookup, @Nonnull String key, long value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putStringArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull String[] arr) {
		return putArrayInternal(lookup, key, delimeter, arr);
	}

	@Override
	public boolean putBooleanArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull boolean[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean putIntArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter, @Nonnull int[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean putFloatArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull float[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean putDoubleArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull double[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean putLongArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull long[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean remove(@Nonnull L lookup, @Nonnull String key) { return removeInternal(lookup, key); }

	@Override
	@Nonnull
	public PropertyType typeOf(L lookup, String key) { return PropertyType.STRING; }

	// ===============================================================================================================

	/**
	 * Get the name of the database.
	 * 
	 * @return Returns the database name
	 */
	@Nonnull
	public String getDatabase() { return database; }

	/**
	 * Get the database table name.
	 * 
	 * @return Returns the table name inside the database
	 */
	@Nonnull
	public String getTable() { return table; }

	/**
	 * Check if provided {@link ExecutorService} is valid for asynchronous
	 * processing.
	 * 
	 * @param service - executor service to use for processing
	 * @return Returns the provided service if it is valid for use. Otherwise will
	 *         return the default executor ({@link #COMMON_POOL})
	 */
	@Nonnull
	private ExecutorService screenExecutor(@Nullable ExecutorService service) {
		return service != null ? service : defaultExecutor;
	}
}
