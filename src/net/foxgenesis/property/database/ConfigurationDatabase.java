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

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	public ConfigurationDatabase(@NotNull String name, @NotNull String database, @NotNull String table,
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
	public ConfigurationDatabase(@NotNull String name, @NotNull String database, @NotNull String table) {
		this(name, database, table, null);
	}

	// ======================================= ASYNC ===============================

	/**
	 * Remove an internal value from the database asynchronously.
	 * 
	 * @param lookup  - property lookup
	 * @param key     - property key
	 * @param service - executor service
	 * 
	 * @return Returns a future that, upon completion, will return {@code true} if
	 *         the property was successfully removed from the database
	 * 
	 * @see #putInternalAsync(Object, String, String, ExecutorService)
	 * @see #getInternalAsync(Object, String, ExecutorService)
	 */
	@NotNull
	protected CompletableFuture<Boolean> removeInternalAsync(@NotNull L lookup, @NotNull String key,
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
	 * 
	 * @return Returns a future that, upon completion, will return {@code true} if
	 *         the property was successfully added/updated in the database
	 * 
	 * @see #removeInternalAsync(Object, String, ExecutorService)
	 * @see #getInternalAsync(Object, String, ExecutorService)
	 */
	@NotNull
	protected CompletableFuture<Boolean> putInternalAsync(@NotNull L lookup, @NotNull String key, @NotNull String value,
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
	 * 
	 * @return Returns the same as
	 *         {@link #putInternalAsync(Object, String, String, ExecutorService)}
	 * 
	 * @see #putInternalAsync(Object, String, String, ExecutorService)
	 */
	@NotNull
	private CompletableFuture<Boolean> putInternalAsync(@NotNull L lookup, @NotNull String key,
			@NotNull Supplier<String> value, @Nullable ExecutorService service) {
		return CompletableFuture.supplyAsync(value, screenExecutor(service))
				.thenCompose(v -> putInternalAsync(lookup, key, v, service));
	}

	/**
	 * Get an internal property inside the database asynchronously.
	 * 
	 * @param lookup  - property lookup
	 * @param key     - property key
	 * @param service - executor service
	 * 
	 * @return Returns a future {@link Optional} containing raw value data
	 * 
	 * @see #removeInternalAsync(Object, String, ExecutorService)
	 * @see #putInternalAsync(Object, String, String, ExecutorService)
	 */
	@NotNull
	protected CompletableFuture<Optional<String>> getInternalAsync(@NotNull L lookup, @NotNull String key,
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
	 * 
	 * @return Returns {@code true} if array was successfully inserted inside the
	 *         database
	 * 
	 * @see #getArrayInternal(Long, String, String, Function, IntFunction)
	 */
	@NotNull
	protected <T> CompletableFuture<Boolean> putArrayInternalAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, @NotNull T[] arr, @Nullable ExecutorService service) {
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
	 * 
	 * @return Returns an {@link Optional} containing a <b>nullable</b> array of
	 *         values
	 * 
	 * @see #putArrayInternal(Long, String, String, Object[])
	 */
	@NotNull
	protected <T> CompletableFuture<Optional<T[]>> getArrayInternalAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @NotNull Function<String, T> map, @NotNull IntFunction<T[]> arr,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApplyAsync(
				o -> o.map(str -> Arrays.stream(str.split(regex)).map(map).toArray(arr)), screenExecutor(service));
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<String>> getStringAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Boolean>> getBooleanAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Boolean::parseBoolean));
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Integer>> getIntAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Integer::parseInt));
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Float>> getFloatAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Float::parseFloat));
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Double>> getDoubleAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Double::parseDouble));
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Long>> getLongAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service) {
		return getInternalAsync(lookup, key, service).thenApply(o -> o.map(Long::parseLong));
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<String[]>> getStringArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Function.identity(), String[]::new, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Boolean::parseBoolean, Boolean[]::new, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Integer::parseInt, Integer[]::new, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Float::parseFloat, Float[]::new, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Double::parseDouble, Double[]::new, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Long[]>> getLongArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service) {
		return getArrayInternalAsync(lookup, key, regex, Long::parseLong, Long[]::new, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putStringAsync(@NotNull L lookup, @NotNull String key, @NotNull String value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, value, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putBooleanAsync(@NotNull L lookup, @NotNull String key, boolean value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putIntAsync(@NotNull L lookup, @NotNull String key, int value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putFloatAsync(@NotNull L lookup, @NotNull String key, float value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putDoubleAsync(@NotNull L lookup, @NotNull String key, double value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putLongAsync(@NotNull L lookup, @NotNull String key, long value,
			@Nullable ExecutorService service) {
		return putInternalAsync(lookup, key, "" + value, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putStringArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, @NotNull String[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, arr, service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putBooleanArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, boolean[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putIntArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, int[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putFloatArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, float[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putDoubleArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, double[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putLongArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, long[] arr, @Nullable ExecutorService service) {
		return putArrayInternalAsync(lookup, key, delimeter, ArrayUtils.toObject(arr), service);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> removeAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service) {
		return removeInternalAsync(lookup, key, service);
	}

	@Override
	@NotNull
	public CompletableFuture<PropertyType> typeOfAsync(L lookup, String key, ExecutorService service) {
		return CompletableFuture.completedFuture(PropertyType.STRING);
	}

	// ======================================= SYNC ================================

	/**
	 * Remove an internal value from the database.
	 * 
	 * @param lookup - property lookup
	 * @param key    - property key
	 * 
	 * @return Returns {@code true} if the property was successfully removed from
	 *         the database
	 * 
	 * @see #putInternal(long, String, String)
	 * @see #getInternal(long, String)
	 */
	protected abstract boolean removeInternal(@NotNull L lookup, @NotNull String key);

	/**
	 * Put/Update an internal property inside the database.
	 * 
	 * @param lookup - property lookup
	 * @param key    - property key
	 * @param value  - value to insert
	 * 
	 * @return Returns {@code true} if the property was successfully added/updated
	 *         in the database
	 * 
	 * @see #removeInternal(long, String)
	 * @see #getInternal(long, String)
	 */
	protected abstract boolean putInternal(@NotNull L lookup, @NotNull String key, @NotNull String value);

	/**
	 * Get an internal property inside the database.
	 * 
	 * @param lookup - property lookup
	 * @param key    - property key
	 * 
	 * @return Returns an {@link Optional} containing raw value data
	 * 
	 * @see #removeInternal(long, String)
	 * @see #putInternal(long, String, String)
	 */
	@NotNull
	protected abstract Optional<String> getInternal(@NotNull L lookup, @NotNull String key);

	/**
	 * Put an array inside the database.
	 * 
	 * @param <T>       Array type
	 * @param lookup    - property lookup
	 * @param key       - property key
	 * @param delimeter - delimiter used to separate values in the array
	 * @param arr       - array to insert
	 * 
	 * @return Returns {@code true} if array was successfully inserted inside the
	 *         database
	 * 
	 * @see #getArrayInternal(Long, String, String, Function, IntFunction)
	 */
	protected <T> boolean putArrayInternal(@NotNull L lookup, @NotNull String key, @NotNull String delimeter,
			@NotNull T[] arr) {
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
	 * 
	 * @return Returns an {@link Optional} containing a <b>nullable</b> array of
	 *         values
	 * 
	 * @see #putArrayInternal(Long, String, String, Object[])
	 */
	@NotNull
	protected <T> Optional<T[]> getArrayInternal(@NotNull L lookup, @NotNull String key, @NotNull String regex,
			@NotNull Function<String, T> map, @NotNull IntFunction<T[]> arr) {
		return getInternal(lookup, key).map(str -> Arrays.stream(str.split(regex)).map(map).toArray(arr));
	}

	@Override
	@NotNull
	public Optional<String> getString(L lookup, String key) {
		return getInternal(lookup, key);
	}

	@Override
	@NotNull
	public Optional<Boolean> getBoolean(L lookup, String key) {
		return getInternal(lookup, key).map(Boolean::parseBoolean);
	}

	@Override
	@NotNull
	public Optional<Integer> getInt(@NotNull L lookup, @NotNull String key) {
		return getInternal(lookup, key).map(Integer::parseInt);
	}

	@Override
	@NotNull
	public Optional<Float> getFloat(@NotNull L lookup, @NotNull String key) {
		return getInternal(lookup, key).map(Float::parseFloat);
	}

	@Override
	@NotNull
	public Optional<Double> getDouble(@NotNull L lookup, @NotNull String key) {
		return getInternal(lookup, key).map(Double::parseDouble);
	}

	@Override
	@NotNull
	public Optional<Long> getLong(@NotNull L lookup, @NotNull String key) {
		return getInternal(lookup, key).map(Long::parseLong);
	}

	@Override
	@NotNull
	public Optional<String[]> getStringArray(@NotNull L lookup, @NotNull String key, @NotNull String regex) {
		return getArrayInternal(lookup, key, regex, Function.identity(), String[]::new);
	}

	@Override
	@NotNull
	public Optional<Boolean[]> getBooleanArray(@NotNull L lookup, @NotNull String key, @NotNull String regex) {
		return getArrayInternal(lookup, key, regex, Boolean::parseBoolean, Boolean[]::new);
	}

	@Override
	@NotNull
	public Optional<Integer[]> getIntArray(@NotNull L lookup, @NotNull String key, @NotNull String regex) {
		return getArrayInternal(lookup, key, regex, Integer::parseInt, Integer[]::new);
	}

	@Override
	@NotNull
	public Optional<Float[]> getFloatArray(@NotNull L lookup, @NotNull String key, @NotNull String regex) {
		return getArrayInternal(lookup, key, regex, Float::parseFloat, Float[]::new);
	}

	@Override
	@NotNull
	public Optional<Double[]> getDoubleArray(@NotNull L lookup, @NotNull String key, @NotNull String regex) {
		return getArrayInternal(lookup, key, regex, Double::parseDouble, Double[]::new);
	}

	@Override
	@NotNull
	public Optional<Long[]> getLongArray(@NotNull L lookup, @NotNull String key, @NotNull String regex) {
		return getArrayInternal(lookup, key, regex, Long::parseLong, Long[]::new);
	}

	@Override
	public boolean putString(@NotNull L lookup, @NotNull String key, @NotNull String value) {
		return putInternal(lookup, key, value);
	}

	@Override
	public boolean putBoolean(@NotNull L lookup, @NotNull String key, boolean value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putInt(@NotNull L lookup, @NotNull String key, int value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putFloat(@NotNull L lookup, @NotNull String key, float value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putDouble(@NotNull L lookup, @NotNull String key, double value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putLong(@NotNull L lookup, @NotNull String key, long value) {
		return putInternal(lookup, key, "" + value);
	}

	@Override
	public boolean putStringArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter,
			@NotNull String[] arr) {
		return putArrayInternal(lookup, key, delimeter, arr);
	}

	@Override
	public boolean putBooleanArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, boolean[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean putIntArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, int[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean putFloatArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, float[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean putDoubleArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, double[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean putLongArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, long[] arr) {
		return putArrayInternal(lookup, key, delimeter, ArrayUtils.toObject(arr));
	}

	@Override
	public boolean remove(@NotNull L lookup, @NotNull String key) {
		return removeInternal(lookup, key);
	}

	@Override
	@NotNull
	public PropertyType typeOf(L lookup, String key) {
		return PropertyType.STRING;
	}

	// ===============================================================================================================

	/**
	 * Get the name of the database.
	 * 
	 * @return Returns the database name
	 */
	@NotNull
	public String getDatabase() {
		return database;
	}

	/**
	 * Get the database table name.
	 * 
	 * @return Returns the table name inside the database
	 */
	@NotNull
	public String getTable() {
		return table;
	}

	/**
	 * Check if provided {@link ExecutorService} is valid for asynchronous
	 * processing.
	 * 
	 * @param service - executor service to use for processing
	 * 
	 * @return Returns the provided service if it is valid for use. Otherwise will
	 *         return the default executor ({@link #COMMON_POOL})
	 */
	@NotNull
	private ExecutorService screenExecutor(@Nullable ExecutorService service) {
		return service != null ? service : defaultExecutor;
	}
}
