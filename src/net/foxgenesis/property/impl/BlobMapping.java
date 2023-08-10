package net.foxgenesis.property.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyMapping;
import net.foxgenesis.property.PropertyType;

import org.apache.commons.lang3.SerializationUtils;

public class BlobMapping implements PropertyMapping {
	private final byte[] data;
	private final long lookup;
	private final PropertyType type;

	public BlobMapping(long lookup, byte[] data, PropertyType type) {
		this.lookup = lookup;
		this.data = Objects.requireNonNull(data);
		this.type = Objects.requireNonNull(type);
	}

	public BlobMapping(long lookup, Blob blob, PropertyType type) throws IOException, SQLException {
		this.lookup = lookup;
		this.type = Objects.requireNonNull(type);
		try (InputStream in = blob.getBinaryStream()) {
			data = in.readAllBytes();
		} finally {
			blob.free();
		}
	}

	@Override
	public String getAsPlainText() {
		if (type == PropertyType.NUMBER)
			return new BigInteger(data).toString();
		return new String(data);
	}

	@Override
	public Object getAsObject() {
		return parse(Object.class);
	}

	@Override
	public String getAsString() {
		if (isUserReadable())
			return getAsPlainText();
		return parse(String.class);
	}

	@Override
	public boolean getAsBoolean() {
		return switch (getType()) {
			case OBJECT -> parse(Boolean.class);
			case PLAIN -> Boolean.parseBoolean(getAsPlainText());
			case NUMBER -> new BigInteger(data).testBit(0);
		};
	}

	@Override
	public int getAsInt() {
		return switch (getType()) {
			case OBJECT -> parse(Integer.class);
			case PLAIN -> Integer.parseInt(getAsPlainText());
			case NUMBER -> new BigInteger(data).intValue();
		};
	}

	@Override
	public float getAsFloat() {
		return switch (getType()) {
			case OBJECT -> parse(Float.class);
			case PLAIN -> Float.parseFloat(getAsPlainText());
			case NUMBER -> new BigInteger(data).floatValue();
		};
	}

	@Override
	public double getAsDouble() {
		return switch (getType()) {
			case OBJECT -> parse(Double.class);
			case PLAIN -> Double.parseDouble(getAsPlainText());
			case NUMBER -> new BigInteger(data).doubleValue();
		};
	}

	@Override
	public long getAsLong() {
		return switch (getType()) {
			case OBJECT -> parse(Long.class);
			case PLAIN -> Long.parseLong(getAsPlainText());
			case NUMBER -> new BigInteger(data).longValue();
		};
	}

	@Override
	public String[] getAsStringArray() {
		if (isUserReadable())
			return unjoin(getAsPlainText(), Function.identity(), String[]::new);
		return parse(String[].class);
	}

	@Override
	public boolean[] getAsBooleanArray() {
		return switch (getType()) {
			case NUMBER -> {
				boolean[] out = new boolean[data.length];
				for (int i = 0; i < data.length; i++)
					out[i] = (data[i] & 1) == 1;
				yield out;
			}
			case OBJECT -> parse(boolean[].class);
			case PLAIN -> unjoinBoolean(getAsPlainText());
		};
	}

	@Override
	public int[] getAsIntegerArray() {
		return switch (getType()) {
			case NUMBER -> {
				ByteBuffer b = ByteBuffer.wrap(data);
				int[] out = new int[data.length / Integer.BYTES];
				for (int i = 0; b.hasRemaining(); i++)
					out[i] = b.getInt();
				yield out;
			}
			case OBJECT -> parse(int[].class);
			case PLAIN -> unjoinInt(getAsPlainText());
		};
	}

	@Override
	public float[] getAsFloatArray() {
		return switch (getType()) {
			case NUMBER -> {
				ByteBuffer b = ByteBuffer.wrap(data);
				float[] out = new float[data.length / Float.BYTES];
				for (int i = 0; b.hasRemaining(); i++)
					out[i] = b.getFloat();
				yield out;
			}
			case OBJECT -> parse(float[].class);
			case PLAIN -> unjoinFloat(getAsPlainText());
		};
	}

	@Override
	public double[] getAsDoubleArray() {
		return switch (getType()) {
			case NUMBER -> {
				ByteBuffer b = ByteBuffer.wrap(data);
				double[] out = new double[data.length / Double.BYTES];
				for (int i = 0; b.hasRemaining(); i++)
					out[i] = b.getDouble();
				yield out;
			}
			case OBJECT -> parse(double[].class);
			case PLAIN -> unjoinDouble(getAsPlainText());
		};
	}

	@Override
	public long[] getAsLongArray() {
		return switch (getType()) {
			case NUMBER -> {
				ByteBuffer b = ByteBuffer.wrap(data);
				long[] out = new long[data.length / Long.BYTES];
				for (int i = 0; b.hasRemaining(); i++)
					out[i] = b.getLong();
				yield out;
			}
			case OBJECT -> parse(long[].class);
			case PLAIN -> unjoinLong(getAsPlainText());
		};
	}

	@Override
	public byte[] getAsByteArray() {
		return switch (getType()) {
			case NUMBER -> Arrays.copyOf(data, data.length);
			case OBJECT -> parse(byte[].class);
			case PLAIN -> unjoinByte(getAsPlainText());
		};
	}

	public boolean isPlainText() {
		return type == PropertyType.PLAIN;
	}

	@Override
	public boolean isUserReadable() {
		return type != PropertyType.OBJECT;
	}

	@Override
	public PropertyType getType() {
		return type;
	}

	@Override
	public long getLookup() {
		return lookup;
	}

	public long getLength() {
		return data.length;
	}

	private byte[] unjoinByte(String text) {
		String[] arr = text.split(",");
		byte[] out = new byte[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Byte.parseByte(arr[i]);
		return out;
	}

	private static boolean[] unjoinBoolean(String text) {
		String[] arr = text.split(",");
		boolean[] out = new boolean[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Boolean.parseBoolean(arr[i]);
		return out;
	}

	private static int[] unjoinInt(String text) {
		String[] arr = text.split(",");
		int[] out = new int[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Integer.parseInt(arr[i]);
		return out;
	}

	private static float[] unjoinFloat(String text) {
		String[] arr = text.split(",");
		float[] out = new float[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Float.parseFloat(arr[i]);
		return out;
	}

	private static double[] unjoinDouble(String text) {
		String[] arr = text.split(",");
		double[] out = new double[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Double.parseDouble(arr[i]);
		return out;
	}

	private static long[] unjoinLong(String text) {
		String[] arr = text.split(",");
		long[] out = new long[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = Long.parseLong(arr[i]);
		return out;
	}

	private static <U> U[] unjoin(String text, Function<String, U> mapper, IntFunction<U[]> construct) {
		return Arrays.stream(text.split(",")).map(mapper).toArray(construct);
	}

	private <U> U parse(Class<U> c) throws PropertyException {
		return c.cast(SerializationUtils.deserialize(data));
	}
}
