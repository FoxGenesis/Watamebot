package net.foxgenesis.property;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;

public interface Property<L, M extends PropertyMapping> extends ImutableProperty<L, M> {
	/**
	 * Set a {@link Serializable} object in the configuration.
	 * 
	 * @param lookup - property lookup
	 * @param obj    - object to store
	 * 
	 * @return Returns {@code true} if the object was stored, {@code false}
	 *         otherwise
	 */
	default boolean set(@NotNull L lookup, @NotNull Serializable obj) {
		return set(lookup, Property.serialize(getInfo(), obj));
	}

	/**
	 * Set an array of bytes in the configuration
	 * 
	 * @param lookup - property lookup
	 * @param data   - array of data to store
	 * 
	 * @return Returns {@code true} if the data was stored, {@code false} otherwise
	 */
	default boolean set(@NotNull L lookup, byte[] data) {
		return set(lookup, new ByteArrayInputStream(data));
	}

	/**
	 * Set a property inside the configuration with the data read from the specified
	 * {@link InputStream}.
	 * 
	 * @param lookup - property lookup
	 * @param in     - stream of data
	 * 
	 * @return Returns {@code true} if the data was stored, {@code false} otherwise
	 */
	boolean set(@NotNull L lookup, @NotNull InputStream in);

	/**
	 * Remove this property from the configuration
	 * 
	 * @param lookup - property lookup
	 * 
	 * @return Returns {@code true} if the property with the specified
	 *         {@code lookup} was removed from the configuration
	 */
	boolean remove(@NotNull L lookup);

	/**
	 * Serialize a object into a byte array.
	 * 
	 * @param info - property information
	 * @param obj  - object to serialize
	 * 
	 * @return Returns the serialized data
	 */
	public static byte[] serialize(@NotNull PropertyInfo info, @NotNull Serializable obj) {
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
		} else if (info.type() == PropertyType.NUMBER) {
			if (obj instanceof Integer i)
				return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(i).array();
			else if (obj instanceof Long f)
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
		return SerializationUtils.serialize(obj);
	}
}
