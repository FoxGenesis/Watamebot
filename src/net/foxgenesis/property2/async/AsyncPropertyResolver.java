package net.foxgenesis.property2.async;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<String>> getStringAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<String>> getStringAsync(@Nonnull L lookup, @Nonnull String key) {
		return getStringAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Boolean>> getBooleanAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Boolean>> getBooleanAsync(@Nonnull L lookup, @Nonnull String key) {
		return getBooleanAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Integer>> getIntAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Integer>> getIntAsync(@Nonnull L lookup, @Nonnull String key) {
		return getIntAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Float>> getFloatAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Float>> getFloatAsync(@Nonnull L lookup, @Nonnull String key) {
		return getFloatAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Double>> getDoubleAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Double>> getDoubleAsync(@Nonnull L lookup, @Nonnull String key) {
		return getDoubleAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Long>> getLongAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Long>> getLongAsync(@Nonnull L lookup, @Nonnull String key) {
		return getLongAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<String[]>> getStringArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<String[]>> getStringArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex) {
		return getStringArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex) {
		return getBooleanArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex) {
		return getIntArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex) {
		return getFloatArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex) {
		return getDoubleArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Long[]>> getLongArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param regex
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Optional<Long[]>> getLongArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String regex) {
		return getLongArrayAsync(lookup, key, regex, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putStringAsync(@Nonnull L lookup, @Nonnull String key, @Nonnull String value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putStringAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String value) {
		return putStringAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putBooleanAsync(@Nonnull L lookup, @Nonnull String key, boolean value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putBooleanAsync(@Nonnull L lookup, @Nonnull String key, boolean value) {
		return putBooleanAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putIntAsync(@Nonnull L lookup, @Nonnull String key, int value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putIntAsync(@Nonnull L lookup, @Nonnull String key, int value) {
		return putIntAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putFloatAsync(@Nonnull L lookup, @Nonnull String key, float value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putFloatAsync(@Nonnull L lookup, @Nonnull String key, float value) {
		return putFloatAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putDoubleAsync(@Nonnull L lookup, @Nonnull String key, double value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putDoubleAsync(@Nonnull L lookup, @Nonnull String key, double value) {
		return putDoubleAsync(lookup, key, value, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putLongAsync(@Nonnull L lookup, @Nonnull String key, long value,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param value
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putLongAsync(@Nonnull L lookup, @Nonnull String key, long value) {
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
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putStringArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull String[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putStringArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull String[] arr) {
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
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putBooleanArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull boolean[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putBooleanArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull boolean[] arr) {
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
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putIntArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull int[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putIntArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull int[] arr) {
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
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putFloatArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull float[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putFloatArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull float[] arr) {
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
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putDoubleArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull double[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putDoubleArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull double[] arr) {
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
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putLongArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull long[] arr, @Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param delimeter
	 * @param arr
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> putLongArrayAsync(@Nonnull L lookup, @Nonnull String key,
			@Nonnull String delimeter, @Nonnull long[] arr) {
		return putLongArrayAsync(lookup, key, delimeter, arr, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> removeAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<Boolean> removeAsync(@Nonnull L lookup, @Nonnull String key) {
		return removeAsync(lookup, key, null);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @param service
	 * @return
	 */
	@Nonnull
	public CompletableFuture<PropertyType> typeOfAsync(@Nonnull L lookup, @Nonnull String key,
			@Nullable ExecutorService service);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param key
	 * @return
	 */
	@Nonnull
	public default CompletableFuture<PropertyType> typeOfAsync(@Nonnull L lookup, @Nonnull String key) {
		return typeOfAsync(lookup, key, null);
	}
}