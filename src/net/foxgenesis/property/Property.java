package net.foxgenesis.property;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;

public interface Property<L, M extends PropertyMapping> extends ImmutableProperty<L, M> {
	/**
	 * Set a {@link Serializable} object in the configuration.
	 *
	 * @param lookup      - property lookup
	 * @param obj         - object to store
	 * @param isUserInput - if this call is by the user
	 *
	 * @return Returns {@code true} if the object was stored, {@code false}
	 *         otherwise
	 */
	default boolean set(@NotNull L lookup, @NotNull Serializable obj, boolean isUserInput) {
		checkUserInput(isUserInput);
		return set(lookup, Property.serialize(getInfo(), obj), isUserInput);
	}

	/**
	 * Set an array of bytes in the configuration
	 *
	 * @param lookup      - property lookup
	 * @param data        - array of data to store
	 * @param isUserInput - if this call is by the user
	 *
	 * @return Returns {@code true} if the data was stored, {@code false} otherwise
	 */
	default boolean set(@NotNull L lookup, byte[] data, boolean isUserInput) {
		checkUserInput(isUserInput);
		return set(lookup, new ByteArrayInputStream(data), isUserInput);
	}

	/**
	 * Set a property inside the configuration with the data read from the specified
	 * {@link InputStream}.
	 *
	 * @param lookup      - property lookup
	 * @param in          - stream of data
	 * @param isUserInput - if this call is by the user
	 *
	 * @return Returns {@code true} if the data was stored, {@code false} otherwise
	 */
	boolean set(@NotNull L lookup, @NotNull InputStream in, boolean isUserInput);

	/**
	 * Remove this property from the configuration
	 *
	 * @param lookup      - property lookup
	 * @param isUserInput - if this call is by the user
	 *
	 * @return Returns {@code true} if the property with the specified
	 *         {@code lookup} was removed from the configuration
	 */
	boolean remove(@NotNull L lookup, boolean isUserInput);

	/**
	 * Check if this property can be modified by user input.
	 * 
	 * @return Returns {@code true} if the user is allowed to modify this property.
	 *         {@code false} otherwise
	 */
	default boolean isUserModifiable() {
		return getInfo().modifiable();
	}

	/**
	 * Method checks if this property is allowed to be modified by the user. If
	 * {@code user} is {@code true} and {@link #isUserModifiable()} is
	 * {@code false}, this method will throw a {@link UnmodifiablePropertyException}
	 * to stop further execution.
	 * 
	 * @param user - if this call is from the user
	 * 
	 * @throws UnmodifiablePropertyException Thrown if {@code user} is {@code true}
	 *                                       and {@link #isUserModifiable()} is
	 *                                       {@code false}
	 */
	default void checkUserInput(boolean user) {
		if (user && !isUserModifiable())
			throw new UnmodifiablePropertyException(
					"Property " + getInfo() + " is not allowed to be modified by user input");
	}

	/**
	 * Serialize a object into a byte array.
	 *
	 * @param info - property information
	 * @param obj  - object to serialize
	 *
	 * @return Returns the serialized data
	 */
	static byte[] serialize(@NotNull PropertyInfo info, @NotNull Serializable obj) {
		// PLAIN TEXT
		if (info.type() == PropertyType.PLAIN) {
			if (obj.getClass().isArray()) {
				Serializable[] a = (Serializable[]) obj;
				StringBuilder b = new StringBuilder();
				for (int i = 0; i < a.length; i++) {
					if (i != 0)
						b.append(',');
					b.append(serialize(info, a[i]));
				}
				return b.toString().getBytes();
			}

			return obj.toString().getBytes();
		}

		// NUMBERS
		if (info.type() == PropertyType.NUMBER) {
			if (obj instanceof Integer i)
				return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(i).array();
			if (obj instanceof Long f)
				return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(f).array();
			else if (obj instanceof Double d)
				return ByteBuffer.allocate(Double.SIZE / Byte.SIZE).putDouble(d).array();
			else if (obj instanceof Float f)
				return ByteBuffer.allocate(Float.SIZE / Byte.SIZE).putFloat(f).array();
			else if (obj instanceof Short s)
				return ByteBuffer.allocate(Short.SIZE / Byte.SIZE).putShort(s).array();
			else if (obj instanceof Boolean f)
				return new byte[] { (byte) (f ? 1 : 0) };
			else if (obj instanceof Byte b)
				return new byte[] { b };
		}

		// JAVA OBJECTS
		return SerializationUtils.serialize(obj);
	}
}
