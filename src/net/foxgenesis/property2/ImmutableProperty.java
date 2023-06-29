package net.foxgenesis.property2;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public interface ImmutableProperty<L> {
	@NotNull
	public Optional<String> getString(@NotNull L lookup);

	@NotNull
	public Optional<Boolean> getBoolean(@NotNull L lookup);

	@NotNull
	public Optional<Integer> getInt(@NotNull L lookup);

	@NotNull
	public Optional<Float> getFloat(@NotNull L lookup);

	@NotNull
	public Optional<Double> getDouble(@NotNull L lookup);

	@NotNull
	public Optional<Long> getLong(@NotNull L lookup);

	@NotNull
	public Optional<String[]> getStringArray(@NotNull L lookup);

	@NotNull
	public Optional<Boolean[]> getBooleanArray(@NotNull L lookup);

	@NotNull
	public Optional<Integer[]> getIntArray(@NotNull L lookup);

	@NotNull
	public Optional<Float[]> getFloatArray(@NotNull L lookup);

	@NotNull
	public Optional<Double[]> getDoubleArray(@NotNull L lookup);

	@NotNull
	public Optional<Long[]> getLongArray(@NotNull L lookup);

	@NotNull
	public PropertyType getType(@NotNull L lookup);
}
