package net.foxgenesis.property3.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Objects;

import net.foxgenesis.property3.PropertyException;
import net.foxgenesis.property3.PropertyMapping;

public class BlobMapping implements PropertyMapping, AutoCloseable {
	private final Blob blob;

	public BlobMapping(Blob blob) {
		this.blob = Objects.requireNonNull(blob);
	}

	@Override
	public Object getAsObject() {
		return parse(ObjectInputStream::readObject);
	}

	@Override
	public String getAsString() throws PropertyException {
		return parse(ObjectInputStream::readUTF);
	}

	@Override
	public boolean getAsBoolean() {
		return parse(ObjectInputStream::readBoolean);
	}

	@Override
	public int getAsInt() {
		return parse(ObjectInputStream::readInt);
	}

	@Override
	public float getAsFloat() {
		return parse(ObjectInputStream::readFloat);
	}

	@Override
	public double getAsDouble() {
		return parse(ObjectInputStream::readDouble);
	}

	@Override
	public long getAsLong() {
		return parse(ObjectInputStream::readLong);
	}

	@Override
	public String[] getAsStringArray() {
		return (String[]) parse(ObjectInputStream::readObject);
	}

	@Override
	public boolean[] getAsBooleanArray() {
		return (boolean[]) parse(ObjectInputStream::readObject);
	}

	@Override
	public int[] getAsIntegerArray() {
		return (int[]) parse(ObjectInputStream::readObject);
	}

	@Override
	public float[] getAsFloatArray() {
		return (float[]) parse(ObjectInputStream::readObject);
	}

	@Override
	public double[] getAsDoubleArray() {
		return (double[]) parse(ObjectInputStream::readObject);
	}

	@Override
	public long[] getAsLongArray() {
		return (long[]) parse(ObjectInputStream::readObject);
	}

	@Override
	public byte[] getAsByteArray() {
		return parse(InputStream::readAllBytes);
	}

	@Override
	public void close() throws SQLException {
		blob.free();
	}

	private <U> U parse(BlobFunction<ObjectInputStream, U> map) throws PropertyException {
		try (ObjectInputStream in = new ObjectInputStream(blob.getBinaryStream())) {
			return map.apply(in);
		} catch (IOException | SQLException | ClassNotFoundException e1) {
			throw new PropertyException(e1);
		} finally {
			try {
				blob.free();
			} catch (SQLException e) {
				throw new PropertyException(e);
			}
		}
	}

	private static interface BlobFunction<I, R> {
		R apply(I in) throws SQLException, IOException, ClassNotFoundException;
	}
}
