package net.foxgenesis.watame.util;

import java.util.Objects;

/**
 * Enumeration for creating Discord timestamps, which can be useful for
 * specifying a date/time across multiple users time zones.
 * <p>
 * Discord timestamps are created by specifying the Unix {@code time} <u>in
 * seconds</u> and the display {@code style}. <blockquote><i>&lt;t : time :
 * style&gt;</i> </blockquote> There are multiple styles to choose from and will
 * display differently based on the user's clock style.
 * </p>
 * <table>
 * <caption>Format table for the Unix timestamp of <b>1543392060</b></caption>
 * <tr>
 * <th>Style</th>
 * <th>Discord Timestamp</th>
 * <th>Output (12-hour clock)</th>
 * <th>Output (24-hour clock)</th>
 * </tr>
 * <tr>
 * <td>Default</td>
 * <td>{@code <t:1543392060>}</td>
 * <td>November 28, 2018 9:01 AM</td>
 * <td>28 November 2018 09:01</td>
 * </tr>
 * <tr>
 * <td>Short Time</td>
 * <td>{@code <t:1543392060:t>}</td>
 * <td>9:01 AM</td>
 * <td>09:01</td>
 * </tr>
 * <tr>
 * <td>Long Time</td>
 * <td>{@code <t:1543392060:T>}</td>
 * <td>9:01:00 AM</td>
 * <td>09:01:00</td>
 * </tr>
 * <tr>
 * <td>Short Date</td>
 * <td>{@code <t:1543392060:d>}</td>
 * <td>11/28/2018</td>
 * <td>28/11/2018</td>
 * </tr>
 * <tr>
 * <td>Long Date</td>
 * <td>{@code <t:1543392060:D>}</td>
 * <td>November 28, 2018</td>
 * <td>28 November 2018</td>
 * </tr>
 * <tr>
 * <td>Short Date/Time</td>
 * <td>{@code <t:1543392060:f>}</td>
 * <td>November 28, 2018 9:01 AM</td>
 * <td>28 November 2018 09:01</td>
 * </tr>
 * <tr>
 * <td>Long Date/Time</td>
 * <td>{@code <t:1543392060:F>}</td>
 * <td>Wednesday, November 28, 2018 9:01 AM</td>
 * <td>Wednesday, 28 November 2018 09:01</td>
 * </tr>
 * <tr>
 * <td>Relative</td>
 * <td>{@code <t:1543392060:R>}</td>
 * <td>3 years ago</td>
 * <td>3 years ago</td>
 * </tr>
 * </table>
 *
 * @author Ashley
 */
public enum TimeFormat {
	/**
	 * The default display style.
	 * <table>
	 * <caption>Output for the Unix time of <b>1543392060</b></caption>
	 * <tr>
	 * <th>Output (12-hour clock)</th>
	 * <th>Output (24-hour clock)</th>
	 * </tr>
	 * <tr>
	 * <td>November 28, 2018 9:01 AM</td>
	 * <td>28 November 2018 09:01</td>
	 * </tr>
	 * </table>
	 *
	 * @see #SHORT_TIME
	 * @see #LONG_TIME
	 * @see #SHORT_DATE
	 * @see #LONG_DATE
	 * @see #SHORT_DATE_TIME
	 * @see #LONG_DATE_TIME
	 * @see #RELATIVE
	 */
	DEFAULT(Character.MIN_VALUE),
	/**
	 * Time is displayed in shorthand form.
	 * <table>
	 * <caption>Output for the Unix time of <b>1543392060</b></caption>
	 * <tr>
	 * <th>Output (12-hour clock)</th>
	 * <th>Output (24-hour clock)</th>
	 * </tr>
	 * <tr>
	 * <td>9:01 AM</td>
	 * <td>09:01</td>
	 * </tr>
	 * </table>
	 *
	 * @see #LONG_TIME
	 */
	SHORT_TIME('t'),
	/**
	 * Time is displayed in hours, minutes and seconds.
	 * <table>
	 * <caption>Output for the Unix time of <b>1543392060</b></caption>
	 * <tr>
	 * <th>Output (12-hour clock)</th>
	 * <th>Output (24-hour clock)</th>
	 * </tr>
	 * <tr>
	 * <td>9:01:00 AM</td>
	 * <td>09:01:00</td>
	 * </tr>
	 * </table>
	 *
	 * @see #SHORT_TIME
	 */
	LONG_TIME('T'),
	/**
	 * The date is displayed in shorthand form.
	 * <table>
	 * <caption>Output for the Unix time of <b>1543392060</b></caption>
	 * <tr>
	 * <th>Output (12-hour clock)</th>
	 * <th>Output (24-hour clock)</th>
	 * </tr>
	 * <tr>
	 * <td>11/28/2018</td>
	 * <td>28/11/2018</td>
	 * </tr>
	 * </table>
	 *
	 * @see #LONG_DATE
	 */
	SHORT_DATE('d'),
	/**
	 * The date is displayed in full.
	 * <table>
	 * <caption>Output for the Unix time of <b>1543392060</b></caption>
	 * <tr>
	 * <th>Output (12-hour clock)</th>
	 * <th>Output (24-hour clock)</th>
	 * </tr>
	 * <tr>
	 * <td>November 28, 2018</td>
	 * <td>28 November 2018</td>
	 * </tr>
	 * </table>
	 *
	 * @see #SHORT_DATE
	 */
	LONG_DATE('D'),
	/**
	 * Time is displayed with short date and time.
	 * <table>
	 * <caption>Output for the Unix time of <b>1543392060</b></caption>
	 * <tr>
	 * <th>Output (12-hour clock)</th>
	 * <th>Output (24-hour clock)</th>
	 * </tr>
	 * <tr>
	 * <td>November 28, 2018 9:01 AM</td>
	 * <td>28 November 2018 09:01</td>
	 * </tr>
	 * </table>
	 *
	 * @see #LONG_DATE_TIME
	 */
	SHORT_DATE_TIME('f'),
	/**
	 * Time is displayed with full date and time.
	 * <table>
	 * <caption>Output for the Unix time of <b>1543392060</b></caption>
	 * <tr>
	 * <th>Output (12-hour clock)</th>
	 * <th>Output (24-hour clock)</th>
	 * </tr>
	 * <tr>
	 * <td>Wednesday, November 28, 2018 9:01 AM</td>
	 * <td>Wednesday, 28 November 2018 09:01</td>
	 * </tr>
	 * </table>
	 *
	 * @see #SHORT_DATE_TIME
	 */
	LONG_DATE_TIME('F'),
	/**
	 * Time is displayed relative to the timestamp.
	 * <table>
	 * <caption>Output for the Unix time of <b>1543392060</b></caption>
	 * <tr>
	 * <th>Output (12-hour clock)</th>
	 * <th>Output (24-hour clock)</th>
	 * </tr>
	 * <tr>
	 * <td>3 years ago</td>
	 * <td>3 years ago</td>
	 * </tr>
	 * </table>
	 */
	RELATIVE('r');

	/**
	 * Style character
	 */
	private final char styleChar;

	/**
	 * Create a new {@link TimeFormat} with the specified {@code style} character.
	 * The default style is represented with the character code of
	 * {@link Character#MIN_VALUE}.
	 *
	 * @param type - character that represents this style
	 */
	TimeFormat(char type) {
		styleChar = Objects.requireNonNull(type);
	}

	/**
	 * Create a new Discord timestamp with the specified Unix {@code time} <u>in seconds</u>.
	 *
	 * @param seconds - seconds since epoch
	 *
	 * @return Returns the created Discord timestamp
	 */
	public String format(long seconds) {
		return "<t:" + seconds + (styleChar != Character.MIN_VALUE ? ':' + styleChar : "") + ">";
	}

	/**
	 * Get the character used to represent this style for formatting.
	 *
	 * @return Returns the character that specifies this style
	 */
	public char getStyleCharacter() {
		return styleChar;
	}

	/**
	 * Get the {@link TimeFormat} expressed by the specified {@code style}
	 * character.
	 *
	 * @param style - style character
	 *
	 * @return Returns the {@link TimeFormat} represented by the specified character
	 *         or {@link #DEFAULT} if none was found
	 */
	public static TimeFormat fromCharacter(char style) {
		for (TimeFormat format : values())
			if (format.styleChar == style)
				return format;
		return DEFAULT;
	}
}
