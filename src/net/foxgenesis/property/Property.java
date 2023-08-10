package net.foxgenesis.property;

import java.io.InputStream;
import java.io.Serializable;

import org.jetbrains.annotations.NotNull;

public interface Property extends ImmutableProperty {
	boolean set(long lookup, Serializable obj);

	boolean set(long lookup, byte[] data);

	boolean set(long lookup, @NotNull InputStream in);

	/**
	 * Remove this property from the configuration
	 * 
	 * @param lookup - property lookup
	 * 
	 * @return Returns {@code true} if the property with the specified
	 *         {@code lookup} was removed from the configuration
	 */
	boolean remove(long lookup);
}
