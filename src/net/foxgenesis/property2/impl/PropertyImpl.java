package net.foxgenesis.property2.impl;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import net.foxgenesis.property2.Property;
import net.foxgenesis.property2.PropertyResolver;
import net.foxgenesis.property2.PropertyType;

public class PropertyImpl<L, R extends PropertyResolver<L>> implements Property<L> {
	protected static final String DELIMETER = ",";

	@NotNull
	protected final String key;

	@NotNull
	protected final R resolver;

	@NotNull
	protected PropertyType type = PropertyType.STRING;

	public PropertyImpl(@NotNull String key, @NotNull R resolver) {
		this.key = Objects.requireNonNull(key);
		this.resolver = Objects.requireNonNull(resolver);

	}

	@Override
	@NotNull
	public Optional<String> getString(L lookup) {
		return resolver.getString(lookup, key);
	}

	@Override
	@NotNull
	public Optional<Boolean> getBoolean(L lookup) {
		return resolver.getBoolean(lookup, key);
	}

	@Override
	@NotNull
	public Optional<Integer> getInt(L lookup) {
		return resolver.getInt(lookup, key);
	}

	@Override
	@NotNull
	public Optional<Float> getFloat(L lookup) {
		return resolver.getFloat(lookup, key);
	}

	@Override
	@NotNull
	public Optional<Double> getDouble(L lookup) {
		return resolver.getDouble(lookup, key);
	}

	@Override
	@NotNull
	public Optional<Long> getLong(L lookup) {
		return resolver.getLong(lookup, key);
	}

	@Override
	@NotNull
	public Optional<String[]> getStringArray(L lookup) {
		return resolver.getStringArray(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public Optional<Boolean[]> getBooleanArray(L lookup) {
		return resolver.getBooleanArray(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public Optional<Integer[]> getIntArray(L lookup) {
		return resolver.getIntArray(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public Optional<Float[]> getFloatArray(L lookup) {
		return resolver.getFloatArray(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public Optional<Double[]> getDoubleArray(L lookup) {
		return resolver.getDoubleArray(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public Optional<Long[]> getLongArray(L lookup) {
		return resolver.getLongArray(lookup, key, DELIMETER);
	}

	@Override
	public boolean putString(@NotNull L lookup, @NotNull String value) {
		return resolver.putString(lookup, key, value);
	}

	@Override
	public boolean putBoolean(@NotNull L lookup, boolean value) {
		return resolver.putBoolean(lookup, key, value);
	}

	@Override
	public boolean putInt(@NotNull L lookup, int value) {
		return resolver.putInt(lookup, key, value);
	}

	@Override
	public boolean putFloat(@NotNull L lookup, float value) {
		return resolver.putFloat(lookup, key, value);
	}

	@Override
	public boolean putDouble(@NotNull L lookup, double value) {
		return resolver.putDouble(lookup, key, value);
	}

	@Override
	public boolean putLong(@NotNull L lookup, long value) {
		return resolver.putLong(lookup, key, value);
	}

	@Override
	public boolean putStringArray(@NotNull L lookup, @NotNull String[] arr) {
		return resolver.putStringArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putBooleanArray(@NotNull L lookup, boolean[] arr) {
		return resolver.putBooleanArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putIntArray(@NotNull L lookup, int[] arr) {
		return resolver.putIntArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putFloatArray(@NotNull L lookup, float[] arr) {
		return resolver.putFloatArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putDoubleArray(@NotNull L lookup, double[] arr) {
		return resolver.putDoubleArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean putLongArray(@NotNull L lookup, long[] arr) {
		return resolver.putLongArray(lookup, key, DELIMETER, arr);
	}

	@Override
	public boolean remove(@NotNull L lookup) {
		return resolver.remove(lookup, key);
	}

	@Override
	@NotNull
	public PropertyType getType(@NotNull L lookup) {
		return PropertyType.STRING;
	}

	@NotNull
	public String getKey() {
		return key;
	}

	@Override
	@NotNull
	public String toString() {
		return "Property[key = " + key + ", type = " + type + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, resolver, type);
	}

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
