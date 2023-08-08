package net.foxgenesis.property3;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Optional;

public interface Property {

	Optional<? extends PropertyMapping> get(long lookup);

	<T extends Serializable> boolean set(long lookup, T obj);

	boolean set(long lookup, String data);

	boolean set(long lookup, boolean bool);

	boolean set(long lookup, int num);

	boolean set(long lookup, float num);

	boolean set(long lookup, double num);

	boolean set(long lookup, long num);

	boolean set(long lookup, byte[] data);

	boolean set(long lookup, InputStream in);

	boolean remove(long lookup);

	boolean isPresent(long lookup);
	
	PropertyInfo getInfo();
}
