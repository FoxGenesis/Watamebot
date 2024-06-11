package net.foxgenesis.util;

import java.util.regex.Pattern;

/**
 * Interface defining the functionality of a custom variable replacement format.
 * 
 * @author Ashley
 */
@FunctionalInterface
public interface Formattable {
	public static final Pattern VARIABLE_PATTERN = Pattern.compile("%([a-zA-Z0-9]+)%");

	/**
	 * Format the specified format string by replacing all occurrences of
	 * {@code %([a-zA-Z0-9]+)%} with the result of {@link #replace(String)}.
	 * 
	 * @param format - string to format
	 * @return Returns the formatted string
	 */
	default String format(String format) {
		return VARIABLE_PATTERN.matcher(format).replaceAll(result -> replace(result.group(1)));
	}

	/**
	 * Replace the specified variable with it's formatted information or otherwise
	 * {@code null}.
	 * 
	 * @param variable - variable name
	 * @return Returns the information of {@code variable} or {@code null} otherwise
	 */
	String replace(String variable);
}
