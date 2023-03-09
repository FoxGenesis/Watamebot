package net.foxgenesis.property2;

import java.util.Optional;

import javax.annotation.Nonnull;

public interface PropertyResolver<L> {

	@Nonnull
	public Optional<String> getString(@Nonnull L lookup, @Nonnull String key);

	@Nonnull
	public Optional<Boolean> getBoolean(@Nonnull L lookup, @Nonnull String key);

	@Nonnull
	public Optional<Integer> getInt(@Nonnull L lookup, @Nonnull String key);

	@Nonnull
	public Optional<Float> getFloat(@Nonnull L lookup, @Nonnull String key);

	@Nonnull
	public Optional<Double> getDouble(@Nonnull L lookup, @Nonnull String key);

	@Nonnull
	public Optional<Long> getLong(@Nonnull L lookup, @Nonnull String key);

	@Nonnull
	public Optional<String[]> getStringArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex);

	@Nonnull
	public Optional<Boolean[]> getBooleanArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex);

	@Nonnull
	public Optional<Integer[]> getIntArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex);

	@Nonnull
	public Optional<Float[]> getFloatArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex);

	@Nonnull
	public Optional<Double[]> getDoubleArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex);

	@Nonnull
	public Optional<Long[]> getLongArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String regex);

	public boolean putString(@Nonnull L lookup, @Nonnull String key, @Nonnull String value);

	public boolean putBoolean(@Nonnull L lookup, @Nonnull String key, boolean value);

	public boolean putInt(@Nonnull L lookup, @Nonnull String key, int value);

	public boolean putFloat(@Nonnull L lookup, @Nonnull String key, float value);

	public boolean putDouble(@Nonnull L lookup, @Nonnull String key, double value);

	public boolean putLong(@Nonnull L lookup, @Nonnull String key, long value);

	public boolean putStringArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull String[] arr);

	public boolean putBooleanArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull boolean[] arr);

	public boolean putIntArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter, @Nonnull int[] arr);

	public boolean putFloatArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull float[] arr);

	public boolean putDoubleArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter,
			@Nonnull double[] arr);

	public boolean putLongArray(@Nonnull L lookup, @Nonnull String key, @Nonnull String delimeter, @Nonnull long[] arr);

	public boolean remove(@Nonnull L lookup, @Nonnull String key);

	@Nonnull
	public PropertyType typeOf(@Nonnull L lookup, @Nonnull String key);
}
