package net.foxgenesis.property;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.apache.commons.lang3.SerializationUtils;

/**
 * Interface defining method of parsing a property value into a usable data
 * type.
 *
 * @author Ashley
 */
public interface PropertyMapping {
	/**
	 * Parse this property as plain text.
	 *
	 * @return Returns the property value as it is inside the configuration
	 */
	String getAsPlainText();

	/**
	 * Parse this property value as a serialized object.
	 *
	 * @return Returns the serialized object
	 */
	Object getAsObject();

	/**
	 * Parse this property value as a string of text.
	 *
	 * @return Returns the value as a string
	 */
	String getAsString();

	/**
	 * Parse this property value as a boolean.
	 *
	 * @return Returns the parsed boolean
	 */
	boolean getAsBoolean();

	/**
	 * Parse this property value as an integer.
	 *
	 * @return Returns the parsed integer
	 */
	int getAsInt();

	/**
	 * Parse this property value as a float.
	 *
	 * @return Returns the parsed float
	 */
	float getAsFloat();

	/**
	 * Parse this property value as a double.
	 *
	 * @return Returns the parsed double.
	 */
	double getAsDouble();

	/**
	 * Parse this property value as a long.
	 *
	 * @return Returns the parsed long.
	 */
	long getAsLong();

	/**
	 * Parse this property value as an array of strings.
	 *
	 * @return Returns the parsed string array
	 */
	String[] getAsStringArray();

	/**
	 * Parse this property value as an array of booleans.
	 *
	 * @return Returns the parsed boolean array
	 */
	boolean[] getAsBooleanArray();

	/**
	 * Parse this property value as an array of bytes.
	 *
	 * @return Returns the parsed byte array
	 */
	byte[] getAsByteArray();

	/**
	 * Parse this property value as an array of integers.
	 *
	 * @return Returns the parsed integer array
	 */
	int[] getAsIntegerArray();

	/**
	 * Parse this property value as an array of floats.
	 *
	 * @return Returns the parsed float array
	 */
	float[] getAsFloatArray();

	/**
	 * Parse this property value as an array of doubles.
	 *
	 * @return Returns the parsed double array
	 */
	double[] getAsDoubleArray();

	/**
	 * Parse this property value as an array of longs.
	 *
	 * @return Returns the parsed long array
	 */
	long[] getAsLongArray();

	/**
	 * Get storage type of this property.
	 *
	 * @return Returns a {@link PropertyType} that defines how a property should be
	 *         stored inside the configuration
	 */
	PropertyType getType();

	/**
	 * Get the size of this property value.
	 *
	 * @return Returns a long representing how large this property is
	 */
	long getLength();

	/**
	 * Check if this property can be displayed in a user readable format.
	 *
	 * @return Returns {@code true} if this property can be displayed to the user.
	 *         {@code false} otherwise.
	 */
	default boolean isUserReadable() {
		return getType() != PropertyType.OBJECT;
	}

	/**
	 * Check if this property is stored as plain text.
	 *
	 * @return Returns {@code true} if this property is stored without any
	 *         additional modifications to it
	 */
	default boolean isPlainText() {
		return getType() == PropertyType.PLAIN;
	}

	static byte[] unjoinByte(String text) {
		String[] arr = text.split(",");
		byte[] out = new byte[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Byte.parseByte(arr[i]);
		return out;
	}

	static boolean[] unjoinBoolean(String text) {
		String[] arr = text.split(",");
		boolean[] out = new boolean[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Boolean.parseBoolean(arr[i]);
		return out;
	}

	static int[] unjoinInt(String text) {
		String[] arr = text.split(",");
		int[] out = new int[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Integer.parseInt(arr[i]);
		return out;
	}

	static float[] unjoinFloat(String text) {
		String[] arr = text.split(",");
		float[] out = new float[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Float.parseFloat(arr[i]);
		return out;
	}

	static double[] unjoinDouble(String text) {
		String[] arr = text.split(",");
		double[] out = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Double.parseDouble(arr[i]);
		return out;
	}

	static long[] unjoinLong(String text) {
		String[] arr = text.split(",");
		long[] out = new long[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Long.parseLong(arr[i]);
		return out;
	}

	static <U> U[] unjoin(String text, Function<String, U> mapper, IntFunction<U[]> construct) {
		return Arrays.stream(text.split(",")).map(mapper).toArray(construct);
	}

	static <U> U parse(Class<U> returnType, byte[] data) throws PropertyException {
		return returnType.cast(SerializationUtils.deserialize(data));
	}
}
