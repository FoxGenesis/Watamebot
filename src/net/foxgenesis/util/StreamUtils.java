package net.foxgenesis.util;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Utility class that contains helpful methods for {@link Stream}.
 *
 * @author Ashley
 *
 */
public final class StreamUtils {
	/**
	 * Returns a possibly parallel stream if the amount of items exceed 1024.
	 *
	 * @param <T> - stream type
	 * @param col - collection to get stream for
	 * 
	 * @return Returns a {@link Stream} that will be <i>possibly</i> parallel if
	 *         {@link Collection#size()} is greater than 1024. Otherwise returns a
	 *         sequential stream
	 * 
	 * @see #getEffectiveStream(Stream)
	 */
	public static <T> Stream<T> getEffectiveStream(Collection<T> col) {
		return col.size() > 1024 ? col.parallelStream() : col.stream();
	}

	/**
	 * Returns a possibly parallel stream if the amount of items exceed 1024.
	 *
	 * @param <T>    - stream type
	 * @param stream - stream to use
	 * 
	 * @return Returns a {@link Stream} that will be <i>possibly</i> parallel if
	 *         {@link Stream#count()} is greater than 1024. Otherwise returns the
	 *         input stream
	 * 
	 * @see #getEffectiveStream(Collection)
	 */
	public static <T> Stream<T> getEffectiveStream(Stream<T> stream) {
		return stream.count() > 1024 ? stream.parallel() : stream;
	}
}
