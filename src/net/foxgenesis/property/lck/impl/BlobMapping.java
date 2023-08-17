package net.foxgenesis.property.lck.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import net.foxgenesis.property.PropertyMapping;
import net.foxgenesis.property.PropertyType;

import org.jetbrains.annotations.NotNull;

public class BlobMapping implements PropertyMapping {
	private final byte[] data;
	private final long lookup;
	private final PropertyType type;

	public BlobMapping(long lookup, byte[] data, @NotNull PropertyType type) {
		this.lookup = lookup;
		this.data = Objects.requireNonNull(data);
		this.type = Objects.requireNonNull(type);
	}

	public BlobMapping(long lookup, @NotNull Blob blob, @NotNull PropertyType type) throws IOException, SQLException {
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
		return PropertyMapping.parse(Object.class, data);
	}

	@Override
	public String getAsString() {
		if (isUserReadable())
			return getAsPlainText();
		return PropertyMapping.parse(String.class, data);
	}

	@Override
	public boolean getAsBoolean() {
		return switch (getType()) {
			case OBJECT -> PropertyMapping.parse(Boolean.class, data);
			case PLAIN -> Boolean.parseBoolean(getAsPlainText());
			case NUMBER -> new BigInteger(data).testBit(0);
		};
	}

	@Override
	public int getAsInt() {
		return switch (getType()) {
			case OBJECT -> PropertyMapping.parse(Integer.class, data);
			case PLAIN -> Integer.parseInt(getAsPlainText());
			case NUMBER -> new BigInteger(data).intValue();
		};
	}

	@Override
	public float getAsFloat() {
		return switch (getType()) {
			case OBJECT -> PropertyMapping.parse(Float.class, data);
			case PLAIN -> Float.parseFloat(getAsPlainText());
			case NUMBER -> new BigInteger(data).floatValue();
		};
	}

	@Override
	public double getAsDouble() {
		return switch (getType()) {
			case OBJECT -> PropertyMapping.parse(Double.class, data);
			case PLAIN -> Double.parseDouble(getAsPlainText());
			case NUMBER -> new BigInteger(data).doubleValue();
		};
	}

	@Override
	public long getAsLong() {
		return switch (getType()) {
			case OBJECT -> PropertyMapping.parse(Long.class, data);
			case PLAIN -> Long.parseLong(getAsPlainText());
			case NUMBER -> new BigInteger(data).longValue();
		};
	}

	@Override
	public String[] getAsStringArray() {
		if (isUserReadable())
			return PropertyMapping.unjoin(getAsPlainText(), Function.identity(), String[]::new);
		return PropertyMapping.parse(String[].class, data);
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
			case OBJECT -> PropertyMapping.parse(boolean[].class, data);
			case PLAIN -> PropertyMapping.unjoinBoolean(getAsPlainText());
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
			case OBJECT -> PropertyMapping.parse(int[].class, data);
			case PLAIN -> PropertyMapping.unjoinInt(getAsPlainText());
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
			case OBJECT -> PropertyMapping.parse(float[].class, data);
			case PLAIN -> PropertyMapping.unjoinFloat(getAsPlainText());
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
			case OBJECT -> PropertyMapping.parse(double[].class, data);
			case PLAIN -> PropertyMapping.unjoinDouble(getAsPlainText());
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
			case OBJECT -> PropertyMapping.parse(long[].class, data);
			case PLAIN -> PropertyMapping.unjoinLong(getAsPlainText());
		};
	}

	@Override
	public byte[] getAsByteArray() {
		return switch (getType()) {
			case NUMBER -> Arrays.copyOf(data, data.length);
			case OBJECT -> PropertyMapping.parse(byte[].class, data);
			case PLAIN -> PropertyMapping.unjoinByte(getAsPlainText());
		};
	}

	@Override
	public PropertyType getType() {
		return type;
	}

	public long getLookup() {
		return lookup;
	}

	@Override
	public long getLength() {
		return data.length;
	}
}
