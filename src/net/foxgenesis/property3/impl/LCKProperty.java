package net.foxgenesis.property3.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import net.foxgenesis.property3.Property;
import net.foxgenesis.property3.PropertyInfo;

public class LCKProperty implements Property {
	private final PropertyInfo info;

	private final LCKPropertyResolver resolver;

	public LCKProperty(PropertyInfo info, LCKPropertyResolver resolver) {
		this.info = Objects.requireNonNull(info);
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Optional<BlobMapping> get(long lookup) {
		return resolver.getInternal(lookup, info).map(BlobMapping::new);
	}

	@Override
	public boolean set(long lookup, byte[] data) {
		return resolver.putInternal(lookup, info, data);
	}

	@Override
	public <T extends Serializable> boolean set(long lookup, T obj) {
		return resolver.putInternal(lookup, info, obj);
	}

	@Override
	public boolean set(long lookup, String data) {
		return resolver.putInternal(lookup, info, data);
	}

	@Override
	public boolean set(long lookup, boolean bool) {
		return resolver.putInternal(lookup, info, bool);
	}

	@Override
	public boolean set(long lookup, int num) {
		return resolver.putInternal(lookup, info, num);
	}

	@Override
	public boolean set(long lookup, float num) {
		return resolver.putInternal(lookup, info, num);
	}

	@Override
	public boolean set(long lookup, double num) {
		return resolver.putInternal(lookup, info, num);
	}

	@Override
	public boolean set(long lookup, long num) {
		return resolver.putInternal(lookup, info, num);
	}

	@Override
	public boolean set(long lookup, InputStream in) {
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
}
