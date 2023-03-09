package net.foxgenesis.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for Strings
 * 
 * @author Ashley
 *
 */
public final class StringUtils {

	// ================================= URLs =====================================

	/**
	 * Compiled regex checking for URL character sequences. This pattern is case
	 * insensitive. <blockquote>
	 * {@code \b((http|https|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])}
	 * </blockquote>
	 */
	public static final Pattern PATTERN_URL = Pattern.compile(
			"\\b((http|https|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Compiled regex checking for URL character sequences. This pattern is case
	 * insensitive and contains named groups for {@code protocol}, {@code domain}
	 * and {@code path}. <blockquote>
	 * {@code \b(?<protocol>http|https|ftp|file)://(?<domain>[-a-zA-Z0-9+&@#%?=~_|!:,.;]*[-a-zA-Z0-9+&@#%=~_|])(?<path>\S*)}
	 * </blockquote>
	 */
	public static final Pattern PATTERN_URL_WITH_GROUPING = Pattern.compile(
			"\\b(?<protocol>http[s]?|ftp|file)://(?<domain>[-a-zA-Z0-9+&@#%?=~_|!:,.;]*[-a-zA-Z0-9+&@#%=~_|])(?<path>\\S*(?<file>(?<=/)\\S*))?",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Predicate that tests if a URL is found in a given input string.
	 */
	public static final Predicate<String> CONTAINS_URL = PATTERN_URL.asPredicate();

	/**
	 * Predicate that tests if the given input string is a URL.
	 */
	public static final Predicate<String> IS_URL = PATTERN_URL.asMatchPredicate();

	/**
	 * Find all URLs within a String and split its components into groups.
	 * 
	 * @param str - String to check
	 * @return A {@link Stream} of {@link MatchResult MatchResults} containing URL
	 *         groups: {@code protocol}({@code 1}), {@code domain}({@code 2}) and
	 *         {@code path}({@code 3})
	 */
	@Nonnull
	public static Stream<MatchResult> findURLWithGroups(@Nonnull String str) {
		return PATTERN_URL_WITH_GROUPING.matcher(str).results();
	}

	/**
	 * Find all occurrences of a {@link URL} in the given string.
	 * 
	 * @param str - string to check
	 * @return Returns a {@link Stream} of {@link URL URLs}
	 */
	@Nonnull
	public static Stream<URL> findURLs(@Nonnull String str) { return findURLs(str, null); }

	/**
	 * Find all occurrences of a {@link URL} in the given string. All
	 * {@link MalformedURLException MalformedURLExceptions} will be consumed by the
	 * {@code errorHandler} while parsing.
	 * 
	 * @param str          - string to check
	 * @param errorHandler - possibly {@code null} error handler
	 * @return Returns a {@link Stream} of {@link URL URLs}
	 */
	@Nonnull
	public static Stream<URL> findURLs(@Nonnull String str, @Nullable Consumer<Exception> errorHandler) {
		return PATTERN_URL.matcher(str).results().map(result -> result.group()).filter(s -> !s.isBlank()).map(t -> {
			try {
				return new URL(t);
			} catch (MalformedURLException e) {
				if (errorHandler != null)
					errorHandler.accept(e);
				return null;
			}
		}).filter(Objects::nonNull);
	}

	// ================================= MISC =====================================

	/**
	 * Limit the length of a string if it exceeds the provided length.
	 * 
	 * @param str    - string to limit
	 * @param length - length limit
	 * @return Returns a {@link String} that does not exceed the given
	 *         {@code length}
	 */
	public static String limit(String str, int length) {
		return str != null && str.length() > length ? str.substring(0, length) : str;
	}
}
