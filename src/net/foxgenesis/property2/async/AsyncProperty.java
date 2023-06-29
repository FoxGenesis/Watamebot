package net.foxgenesis.property2.async;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import net.foxgenesis.property2.Property;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 * @param <L>
 */
public interface AsyncProperty<L> extends AsyncImmutableProperty<L>, Property<L> {
	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putStringAsync(@NotNull L lookup, @NotNull String value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putBooleanAsync(@NotNull L lookup, boolean value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putIntAsync(@NotNull L lookup, int value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putFloatAsync(@NotNull L lookup, float value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putDoubleAsync(@NotNull L lookup, double value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putLongAsync(@NotNull L lookup, long value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putStringArrayAsync(@NotNull L lookup, @NotNull String[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putBooleanArrayAsync(@NotNull L lookup, boolean[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putIntArrayAsync(@NotNull L lookup, int[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putFloatArrayAsync(@NotNull L lookup, float[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putDoubleArrayAsync(@NotNull L lookup, double[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> putLongArrayAsync(@NotNull L lookup, long[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Boolean> removeAsync(@NotNull L lookup);
}
