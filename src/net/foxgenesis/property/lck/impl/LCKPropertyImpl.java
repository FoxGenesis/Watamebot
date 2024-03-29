package net.foxgenesis.property.lck.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import net.foxgenesis.property.Property;
import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.lck.LCKProperty;
import net.foxgenesis.property.lck.LCKPropertyResolver;

public class LCKPropertyImpl implements LCKProperty {
	private final PropertyInfo info;

	protected final LCKPropertyResolver resolver;

	public LCKPropertyImpl(PropertyInfo info, LCKPropertyResolver resolver) {
		this.info = Objects.requireNonNull(info);
		this.resolver = Objects.requireNonNull(resolver);
	}

	@SuppressWarnings("null")
	@Override
	public Optional<BlobMapping> get(Long lookup) {
		return resolver.getInternal(lookup, info).map(t -> {
			try {
				return new BlobMapping(lookup, t, getInfo().type());
			} catch (IOException | SQLException e) {
				throw new PropertyException(e);
			}
		});
	}

	@Override
	public boolean set(Long lookup, Serializable obj, boolean isUserInput) {
		checkUserInput(isUserInput);
		return set(lookup, Property.serialize(getInfo(), obj), isUserInput);
	}

	@Override
	public boolean set(Long lookup, byte[] data, boolean isUserInput) {
		checkUserInput(isUserInput);
		return set(lookup, new ByteArrayInputStream(data), isUserInput);
	}

	@Override
	public boolean set(Long lookup, InputStream in, boolean isUserInput) {
		checkUserInput(isUserInput);
		return resolver.putInternal(lookup, info, in);
	}

	@Override
	public boolean remove(Long lookup, boolean isUserInput) {
		checkUserInput(isUserInput);
		return resolver.removeInternal(lookup, info);
	}

	@Override
	public boolean isPresent(Long lookup) {
		return resolver.isPresent(lookup, info);
	}

	@Override
	public PropertyInfo getInfo() {
		return info;
	}

	@Override
	public int hashCode() {
		return Objects.hash(info, resolver);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		LCKPropertyImpl other = (LCKPropertyImpl) obj;
		return Objects.equals(info, other.info) && Objects.equals(resolver, other.resolver);
	}

	@Override
	public String toString() {
		return "LCKProperty [" + (info != null ? "info=" + info + ", " : "")
				+ (resolver != null ? "resolver=" + resolver : "") + "]";
	}
}
