package net.foxgenesis.util;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ArrayUtils {

	public static <T> String commaSeparated(T[] arr) {
		return arr == null ? null : sep(Arrays.stream(arr));
	}

	@SuppressWarnings("rawtypes")
	public static String commaSeparated(int[] arr) {
		return arr == null ? null : sep((Stream) Arrays.stream(arr));
	}

	private static String sep(Stream<?> s) {
		return s.map(a -> a + "").collect(Collectors.joining(","));
	}
}
