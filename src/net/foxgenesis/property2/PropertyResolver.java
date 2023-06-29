package net.foxgenesis.property2;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public interface PropertyResolver<L> {

	@NotNull
	public Optional<String> getString(@NotNull L lookup, @NotNull String key);

	@NotNull
	public Optional<Boolean> getBoolean(@NotNull L lookup, @NotNull String key);

	@NotNull
	public Optional<Integer> getInt(@NotNull L lookup, @NotNull String key);

	@NotNull
	public Optional<Float> getFloat(@NotNull L lookup, @NotNull String key);

	@NotNull
	public Optional<Double> getDouble(@NotNull L lookup, @NotNull String key);

	@NotNull
	public Optional<Long> getLong(@NotNull L lookup, @NotNull String key);

	@NotNull
	public Optional<String[]> getStringArray(@NotNull L lookup, @NotNull String key, @NotNull String regex);

	@NotNull
	public Optional<Boolean[]> getBooleanArray(@NotNull L lookup, @NotNull String key, @NotNull String regex);

	@NotNull
	public Optional<Integer[]> getIntArray(@NotNull L lookup, @NotNull String key, @NotNull String regex);

	@NotNull
	public Optional<Float[]> getFloatArray(@NotNull L lookup, @NotNull String key, @NotNull String regex);

	@NotNull
	public Optional<Double[]> getDoubleArray(@NotNull L lookup, @NotNull String key, @NotNull String regex);

	@NotNull
	public Optional<Long[]> getLongArray(@NotNull L lookup, @NotNull String key, @NotNull String regex);

	public boolean putString(@NotNull L lookup, @NotNull String key, @NotNull String value);

	public boolean putBoolean(@NotNull L lookup, @NotNull String key, boolean value);

	public boolean putInt(@NotNull L lookup, @NotNull String key, int value);

	public boolean putFloat(@NotNull L lookup, @NotNull String key, float value);

	public boolean putDouble(@NotNull L lookup, @NotNull String key, double value);

	public boolean putLong(@NotNull L lookup, @NotNull String key, long value);

	public boolean putStringArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter,
			@NotNull String[] arr);

	public boolean putBooleanArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, boolean[] arr);

	public boolean putIntArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, int[] arr);

	public boolean putFloatArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, float[] arr);

	public boolean putDoubleArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, double[] arr);

	public boolean putLongArray(@NotNull L lookup, @NotNull String key, @NotNull String delimeter, long[] arr);

	public boolean remove(@NotNull L lookup, @NotNull String key);

	@NotNull
	public PropertyType typeOf(@NotNull L lookup, @NotNull String key);
}
