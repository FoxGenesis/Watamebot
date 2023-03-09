package net.foxgenesis.property2.async;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

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
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putStringAsync(@Nonnull L lookup, @Nonnull String value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putBooleanAsync(@Nonnull L lookup, boolean value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putIntAsync(@Nonnull L lookup, int value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putFloatAsync(@Nonnull L lookup, float value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putDoubleAsync(@Nonnull L lookup, double value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param value
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putLongAsync(@Nonnull L lookup, long value);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putStringArrayAsync(@Nonnull L lookup, @Nonnull String[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putBooleanArrayAsync(@Nonnull L lookup, @Nonnull boolean[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putIntArrayAsync(@Nonnull L lookup, @Nonnull int[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putFloatArrayAsync(@Nonnull L lookup, @Nonnull float[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putDoubleArrayAsync(@Nonnull L lookup, @Nonnull double[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @param arr
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> putLongArrayAsync(@Nonnull L lookup, @Nonnull long[] arr);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Boolean> removeAsync(@Nonnull L lookup);
}
