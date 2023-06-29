package net.foxgenesis.property2.async;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

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
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<String>> getStringAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Boolean>> getBooleanAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Integer>> getIntAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Float>> getFloatAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Double>> getDoubleAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Long>> getLongAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<String[]>> getStringArrayAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param lookup
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<Optional<Long[]>> getLongArrayAsync(@NotNull L lookup);

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	@NotNull
	public CompletableFuture<PropertyType> getTypeAsync(@NotNull L lookup);
}
