package net.foxgenesis.property2.async;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import net.foxgenesis.property2.PropertyType;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 * @param <L> Property lookup type
 */
public interface AsyncImmutableProperty<L> {
	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<String>> getStringAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Boolean>> getBooleanAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Integer>> getIntAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Float>> getFloatAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Double>> getDoubleAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Long>> getLongAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<String[]>> getStringArrayAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * @return
	 */
	@Nonnull
	public CompletableFuture<Optional<Long[]>> getLongArrayAsync(@Nonnull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	@Nonnull
	public CompletableFuture<PropertyType> getTypeAsync(@Nonnull L lookup);
}
