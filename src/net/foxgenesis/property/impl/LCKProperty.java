package net.foxgenesis.property.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import net.foxgenesis.property.Property;
import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyMapping;
import net.foxgenesis.property.PropertyType;

import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;

public class LCKProperty implements Property {
	private final PropertyInfo info;

	protected final LCKPropertyResolver resolver;

	public LCKProperty(PropertyInfo info, LCKPropertyResolver resolver) {
		this.info = Objects.requireNonNull(info);
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Optional<? extends PropertyMapping> get(long lookup) {
		return resolver.getInternal(lookup, info).map(t -> {
			try {
				return new BlobMapping(lookup, t, getInfo().type());
			} catch (IOException | SQLException e) {
				throw new PropertyException(e);
			}
		});
	}

	@Override
	public boolean set(long lookup, Serializable obj) {
		return set(lookup, serialize(getInfo(), obj));
	}

	@Override
	public boolean set(long lookup, byte[] data) {
		return set(lookup, new ByteArrayInputStream(data));
	}

	@Override
	public boolean set(long lookup, @NotNull InputStream in) {
		return resolver.putInternal(lookup, info, in);
	}

	@Override
	public boolean remove(long lookup) {
		return resolver.removeInternal(lookup, info);
	}

	@Override
	public boolean isPresent(long lookup) {
		return resolver.isPresent(lookup, info);
	}

	@Override
	public PropertyInfo getInfo() {
		return info;
	}

	public String getCategory() {
		return info.category();
	}

	public String getKey() {
		return info.name();
	}

	public boolean isUserEditable() {
		return info.modifiable();
	}

	@Override
	public int hashCode() {
		return Objects.hash(info, resolver);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LCKProperty other = (LCKProperty) obj;
		return Objects.equals(info, other.info) && Objects.equals(resolver, other.resolver);
	}

	@Override
	public String toString() {
		return "LCKProperty [" + (info != null ? "info=" + info + ", " : "")
				+ (resolver != null ? "resolver=" + resolver : "") + "]";
	}

	private static byte[] serialize(PropertyInfo info, Serializable obj) {
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
//			else if (obj instanceof String s)
//				return s.getBytes();
//			else if (obj instanceof Boolean b)
//				return b.toString().getBytes();
//			else if(obj instanceof Number n)
//				return n.toString().getBytes();
//			return ("" + obj).getBytes();
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
