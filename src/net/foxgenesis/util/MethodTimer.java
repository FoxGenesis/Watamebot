package net.foxgenesis.util;


/**
 * Utility class that check the amount
 * of time it takes to run a method
 * @author Ashley
 *
 */
public final class MethodTimer {
	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}.
	 * Time is calculated in nano seconds and returned as milliseconds.
	 * @param r - {@link Runnable} to time
	 * @return elapsed time in milliseconds
	 * @see #run(Runnable, int)
	 */
	public static double run(Runnable r) {
		long n = System.nanoTime();
		r.run();
		return (System.nanoTime() - n) / 1_000_000D;
	}
	
	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}, 
	 * {@code n} amount of times. Then calculate the average elapsed
	 * time taken.
	 * @param r - {@link Runnable} to time
	 * @param n - Amount of times to run
	 * @return average elapsed time milliseconds
	 * @see #run(Runnable)
	 */
	public static double run(Runnable r, int n) {
		long count = 0;
		double sum = 0;
		for(int i=0; i< n; i++) {
			System.out.printf("===== Run #%s =====\n",i + 1);
			sum+=run(r);
			count++;
		}
		return sum / count;
	}

	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}.
	 * Time is calculated in nano seconds and returned as a formatted
	 * string with two decimal places.
	 * <p>
	 * This method is effectively equivalent to
	 * <blockquote><pre>
	 * MethodTimer.runFormatMS(r, 2)
	 * </pre></blockquote>
	 * </p>
	 * @param r - {@link Runnable} to time
	 * @return formatted string with two decimal places
	 * @see #runFormatMS(Runnable, int)
	 * @see #runFormatMS(Runnable, int, int)
	 */
	public static String runFormatMS(Runnable r) {
		return runFormatMS(r,2);
	}

	
	/**
	 * Time how long it takes to execute {@link Runnable} {@code r}.
	 * Time is calculated in nano seconds and returned as a formatted
	 * string with {@code decimals} decimal places.
	 * <p>
	 * This method is effectively equivalent to
	 * <blockquote><pre>
	 * String.format("%." + decimals + "f ms", run(r))
	 * </pre></blockquote>
	 * </p>
	 * @param r - {@link Runnable} to time
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
	 * {@code n} amount of times. 
	 * Time is calculated in nano seconds and returned as a formatted
	 * string with {@code decimals} decimal places.
	 * <p>
	 * This method is effectively equivalent to
	 * <blockquote><pre>
	 * String.format("%." + decimals + "f ms", run(r,n));
	 * </pre></blockquote>
	 * </p>
	 * @param r - {@link Runnable} to time
	 * @param n - Amount of times to execute {@code r}
	 * @param decimals - Amount of decimal places to format
	 * @return formatted string with {@code decimals} decimal places
	 * @see #runFormatMS(Runnable)
	 * @see #runFormatMS(Runnable, int, int)
	 */
	public static String runFormatMS(Runnable r, int n, int decimals) {
		return String.format("%." + decimals + "f ms", run(r,n));
	}
}
