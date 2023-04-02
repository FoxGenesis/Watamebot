package net.foxgenesis.util;

import java.util.Arrays;

/**
 * Utility class that check the amount of time it takes to run a method
 * 
 * @author Ashley
 *
 */
public final class MethodTimer {
	
	public static double runNano(Runnable r) {
		long n = System.nanoTime();
		r.run();
		n = System.nanoTime() - n;
		return n;
	}
	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}. Time is
	 * calculated in nano seconds and returned as milliseconds.
	 * 
	 * @param r - {@link Runnable} to time
	 * @return elapsed time in milliseconds
	 * @see #run(Runnable, int)
	 */
	public static double run(Runnable r) {
		return runNano(r) / 1_000_000D;
	}

	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}, {@code n}
	 * amount of times. Then calculate the average elapsed time taken.
	 * 
	 * @param r - {@link Runnable} to time
	 * @param n - Amount of times to run
	 * @return average elapsed time milliseconds
	 * @see #run(Runnable)
	 */
	public static double run(Runnable r, int n) {
		double[] s = new double[n];
		for (int i = 0; i < n; i++) {
			System.out.printf("===== Run #%s =====\n", i + 1);
			s[i] = runNano(r);
			System.out.printf("==== %,.2fms ====\n", s[i] / 1_000_000D);
		}
		return Arrays.stream(s).reduce((a,b) -> (a + b) / 2D).orElseThrow() / 1_000_000D;
		//return (sum / n) / 1_000_000D;
	}

	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}. Time is
	 * calculated in nano seconds and returned as a formatted string with two
	 * decimal places.
	 * <p>
	 * This method is effectively equivalent to <blockquote>
	 * 
	 * <pre>
	 * MethodTimer.runFormatMS(r, 2)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param r - {@link Runnable} to time
	 * @return formatted string with two decimal places
	 * @see #runFormatMS(Runnable, int)
	 * @see #runFormatMS(Runnable, int, int)
	 */
	public static String runFormatMS(Runnable r) { return runFormatMS(r, 2); }

	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}. Time is
	 * calculated in nano seconds and returned as a formatted string with
	 * {@code decimals} decimal places.
	 * <p>
	 * This method is effectively equivalent to <blockquote>
	 * 
	 * <pre>
	 * String.format("%." + decimals + "f ms", run(r))
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param r        - {@link Runnable} to time
	 * @param decimals - Amount of decimal places to format
	 * @return formatted string with {@code decimals} decimal places
	 * @see #runFormatMS(Runnable)
	 * @see #runFormatMS(Runnable, int, int)
	 */
	public static String runFormatMS(Runnable r, int decimals) {
		return String.format("%." + decimals + "f ms", run(r));
	}

	/**
	 * Time the average elapsed time it takes to execute {@link Runnable} {@code r},
	 * {@code n} amount of times. Time is calculated in nano seconds and returned as
	 * a formatted string with {@code decimals} decimal places.
	 * <p>
	 * This method is effectively equivalent to <blockquote>
	 * 
	 * <pre>
	 * String.format("%." + decimals + "f ms", run(r, n));
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param r        - {@link Runnable} to time
	 * @param n        - Amount of times to execute {@code r}
	 * @param decimals - Amount of decimal places to format
	 * @return formatted string with {@code decimals} decimal places
	 * @see #runFormatMS(Runnable)
	 * @see #runFormatMS(Runnable, int, int)
	 */
	public static String runFormatMS(Runnable r, int n, int decimals) {
		return String.format("%." + decimals + "f ms", run(r, n));
	}

	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}. Time is
	 * calculated in nano seconds and returned as a formatted string with two
	 * decimal places.
	 * <p>
	 * This method is effectively equivalent to <blockquote>
	 * 
	 * <pre>
	 * MethodTimer.runFormatMS(r, 2)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param r - {@link Runnable} to time
	 * @return formatted string with two decimal places
	 * @see #runFormatMS(Runnable, int)
	 * @see #runFormatMS(Runnable, int, int)
	 */
	public static String runFormatSec(Runnable r) { return runFormatSec(r, 2); }

	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}. Time is
	 * calculated in nano seconds and returned as a formatted string with
	 * {@code decimals} decimal places.
	 * <p>
	 * This method is effectively equivalent to <blockquote>
	 * 
	 * <pre>
	 * String.format("%." + decimals + "f ms", run(r))
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param r        - {@link Runnable} to time
	 * @param decimals - Amount of decimal places to format
	 * @return formatted string with {@code decimals} decimal places
	 * @see #runFormatMS(Runnable)
	 * @see #runFormatMS(Runnable, int, int)
	 */
	public static String runFormatSec(Runnable r, int decimals) {
		return String.format("%." + decimals + "f second(s)", run(r) / 1000D);
	}

	/**
	 * Time the average elapsed time it takes to execute {@link Runnable} {@code r},
	 * {@code n} amount of times. Time is calculated in nano seconds and returned as
	 * a formatted string with {@code decimals} decimal places.
	 * <p>
	 * This method is effectively equivalent to <blockquote>
	 * 
	 * <pre>
	 * String.format("%." + decimals + "f ms", run(r, n));
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param r        - {@link Runnable} to time
	 * @param n        - Amount of times to execute {@code r}
	 * @param decimals - Amount of decimal places to format
	 * @return formatted string with {@code decimals} decimal places
	 * @see #runFormatMS(Runnable)
	 * @see #runFormatMS(Runnable, int, int)
	 */
	public static String runFormatSec(Runnable r, int n, int decimals) {
		return String.format("%." + decimals + "f second(s)", run(r, n) / 1000D);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param time
	 * @return
	 */
	public static String formatToMilli(long time) { return formatToMilli(time, 2); }

	/**
	 * NEED_JAVADOC
	 * 
	 * @param time
	 * @param decimals
	 * @return
	 */
	public static String formatToMilli(long time, int decimals) { return format(time, decimals, 1_000_000D); }

	/**
	 * NEED_JAVADOC
	 * 
	 * @param time
	 * @param timeOutput
	 * @return
	 */
	public static String formatToSeconds(long time) { return formatToSeconds(time, 2); }

	/**
	 * NEED_JAVADOC
	 * 
	 * @param time
	 * @param timeOutput
	 * @param decimals
	 * @return
	 */
	public static String formatToSeconds(long time, int decimals) { return format(time, decimals, 1_000_000_000D); }

	/**
	 * NEED_JAVADOC
	 * 
	 * @param time
	 * @param decimals
	 * @param div
	 * @return
	 */
	public static String format(long time, int decimals, double div) {
		return String.format("%,." + decimals + "f", time / div);
	}
}
