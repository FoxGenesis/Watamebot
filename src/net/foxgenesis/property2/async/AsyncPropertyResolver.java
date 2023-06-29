package net.foxgenesis.property2.async;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.foxgenesis.property2.PropertyResolver;
import net.foxgenesis.property2.PropertyType;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 * @param <L> Property lookup type
 */
public interface AsyncPropertyResolver<L> extends PropertyResolver<L> {

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<String>> getStringAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<String>> getStringAsync(@NotNull L lookup, @NotNull String key) {
		return getStringAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Boolean>> getBooleanAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Boolean>> getBooleanAsync(@NotNull L lookup, @NotNull String key) {
		return getBooleanAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Integer>> getIntAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Integer>> getIntAsync(@NotNull L lookup, @NotNull String key) {
		return getIntAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Float>> getFloatAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Float>> getFloatAsync(@NotNull L lookup, @NotNull String key) {
		return getFloatAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Double>> getDoubleAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Double>> getDoubleAsync(@NotNull L lookup, @NotNull String key) {
		return getDoubleAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Long>> getLongAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Long>> getLongAsync(@NotNull L lookup, @NotNull String key) {
		return getLongAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<String[]>> getStringArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<String[]>> getStringArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex) {
		return getStringArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex) {
		return getBooleanArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex) {
		return getIntArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex) {
		return getFloatArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex) {
		return getDoubleArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Long[]>> getLongArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Optional<Long[]>> getLongArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String regex) {
		return getLongArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putStringAsync(@NotNull L lookup, @NotNull String key, @NotNull String value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putStringAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String value) {
		return putStringAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putBooleanAsync(@NotNull L lookup, @NotNull String key, boolean value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putBooleanAsync(@NotNull L lookup, @NotNull String key, boolean value) {
		return putBooleanAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putIntAsync(@NotNull L lookup, @NotNull String key, int value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putIntAsync(@NotNull L lookup, @NotNull String key, int value) {
		return putIntAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putFloatAsync(@NotNull L lookup, @NotNull String key, float value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putFloatAsync(@NotNull L lookup, @NotNull String key, float value) {
		return putFloatAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putDoubleAsync(@NotNull L lookup, @NotNull String key, double value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putDoubleAsync(@NotNull L lookup, @NotNull String key, double value) {
		return putDoubleAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putLongAsync(@NotNull L lookup, @NotNull String key, long value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putLongAsync(@NotNull L lookup, @NotNull String key, long value) {
		return putLongAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putStringArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, @NotNull String[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putStringArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, @NotNull String[] arr) {
		return putStringArrayAsync(lookup, key, delimeter, arr, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putBooleanArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, boolean[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putBooleanArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, boolean[] arr) {
		return putBooleanArrayAsync(lookup, key, delimeter, arr, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putIntArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, int[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putIntArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, int[] arr) {
		return putIntArrayAsync(lookup, key, delimeter, arr, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putFloatArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, float[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putFloatArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, float[] arr) {
		return putFloatArrayAsync(lookup, key, delimeter, arr, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putDoubleArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, double @NotNull [] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putDoubleArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, double[] arr) {
		return putDoubleArrayAsync(lookup, key, delimeter, arr, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putLongArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, long @NotNull [] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> putLongArrayAsync(@NotNull L lookup, @NotNull String key,
			@NotNull String delimeter, long[] arr) {
		return putLongArrayAsync(lookup, key, delimeter, arr, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> removeAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<Boolean> removeAsync(@NotNull L lookup, @NotNull String key) {
		return removeAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<PropertyType> typeOfAsync(@NotNull L lookup, @NotNull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * 
	 * @return
	 */
	@NotNull
	public default CompletableFuture<PropertyType> typeOfAsync(@NotNull L lookup, @NotNull String key) {
		return typeOfAsync(lookup, key, null);
	}
}