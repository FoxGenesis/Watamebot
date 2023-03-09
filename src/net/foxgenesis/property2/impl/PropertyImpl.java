package net.foxgenesis.property2.impl;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.foxgenesis.property2.Property;
import net.foxgenesis.property2.PropertyResolver;
import net.foxgenesis.property2.PropertyType;

public class PropertyImpl<L, R extends PropertyResolver<L>> implements Property<L> {
	protected static final String DELIMETER = ",";

	@Nonnull
	protected final String key;

	@Nonnull
	protected final R resolver;

	@Nonnull
	protected PropertyType type = PropertyType.STRING;

	public PropertyImpl(@Nonnull String key, @Nonnull R resolver) {
		this.key = Objects.requireNonNull(key);
		this.resolver = Objects.requireNonNull(resolver);
		
	}

	@Override
	@Nonnull
	public Optional<String> getString(L lookup) { return resolver.getString(lookup, key); }

	@Override
	@Nonnull
	public Optional<Boolean> getBoolean(L lookup) { return resolver.getBoolean(lookup, key); }

	@Override
	@Nonnull
	public Optional<Integer> getInt(L lookup) { return resolver.getInt(lookup, key); }

	@Override
	@Nonnull
	public Optional<Float> getFloat(L lookup) { return resolver.getFloat(lookup, key); }

	@Override
	@Nonnull
	public Optional<Double> getDouble(L lookup) { return resolver.getDouble(lookup, key); }

	@Override
	@Nonnull
	public Optional<Long> getLong(L lookup) { return resolver.getLong(lookup, key); }

	@Override
	@Nonnull
	public Optional<String[]> getStringArray(L lookup) { return resolver.getStringArray(lookup, key, DELIMETER); }

	@Override
	@Nonnull
	public Optional<Boolean[]> getBooleanArray(L lookup) { return resolver.getBooleanArray(lookup, key, DELIMETER); }

	@Override
	@Nonnull
	public Optional<Integer[]> getIntArray(L lookup) { return resolver.getIntArray(lookup, key, DELIMETER); }

	@Override
	@Nonnull
	public Optional<Float[]> getFloatArray(L lookup) { return resolver.getFloatArray(lookup, key, DELIMETER); }

	@Override
	@Nonnull
	public Optional<Double[]> getDoubleArray(L lookup) { return resolver.getDoubleArray(lookup, key, DELIMETER); }

	@Override
	@Nonnull
	public Optional<Long[]> getLongArray(L lookup) { return resolver.getLongArray(lookup, key, DELIMETER); }

	@Override
	public boolean putString(@Nonnull L lookup, @Nonnull String value) {
		return resolver.putString(lookup, key, value);
	}

	@Override
	public boolean putBoolean(@Nonnull L lookup, boolean value) { return resolver.putBoolean(lookup, key, value); }

	@Override
	public boolean putInt(@Nonnull L lookup, int value) { return resolver.putInt(lookup, key, value); }

	@Override
	public boolean putFloat(@Nonnull L lookup, float value) { return resolver.putFloat(lookup, key, value); }

	@Override
	public boolean putDouble(@Nonnull L lookup, double value) { return resolver.putDouble(lookup, key, value); }

	@Override
	public boolean putLong(@Nonnull L lookup, long value) { return resolver.putLong(lookup, key, value); }

	@Override
	public boolean putStringArray(@Nonnull L lookup, @Nonnull String[] arr) {
		return resolver.putStringArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putBooleanArray(@Nonnull L lookup, @Nonnull boolean[] arr) {
		return resolver.putBooleanArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putIntArray(@Nonnull L lookup, @Nonnull int[] arr) {
		return resolver.putIntArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putFloatArray(@Nonnull L lookup, @Nonnull float[] arr) {
		return resolver.putFloatArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putDoubleArray(@Nonnull L lookup, @Nonnull double[] arr) {
		return resolver.putDoubleArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putLongArray(@Nonnull L lookup, @Nonnull long[] arr) {
		return resolver.putLongArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean remove(@Nonnull L lookup) { return resolver.remove(lookup, key); }

	@Override
	@Nonnull
	public PropertyType getType(@Nonnull L lookup) { return PropertyType.STRING; }

	@Nonnull
	public String getKey() { return key; }

	@Override
	@Nonnull
	public String toString() { return "Property[key = " + key + ", type = " + type + "]"; }

	@Override
	public int hashCode() { return Objects.hash(key, resolver, type); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyImpl<?, ?> other = (PropertyImpl<?, ?>) obj;
		return Objects.equals(key, other.key) && Objects.equals(resolver, other.resolver) && type == other.type;
	}
}
