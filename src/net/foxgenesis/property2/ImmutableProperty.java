package net.foxgenesis.property2;

import java.util.Optional;

import javax.annotation.Nonnull;

public interface ImmutableProperty<L> {
	@Nonnull
	public Optional<String> getString(@Nonnull L lookup);

	@Nonnull
	public Optional<Boolean> getBoolean(@Nonnull L lookup);

	@Nonnull
	public Optional<Integer> getInt(@Nonnull L lookup);

	@Nonnull
	public Optional<Float> getFloat(@Nonnull L lookup);

	@Nonnull
	public Optional<Double> getDouble(@Nonnull L lookup);

	@Nonnull
	public Optional<Long> getLong(@Nonnull L lookup);

	@Nonnull
	public Optional<String[]> getStringArray(@Nonnull L lookup);

	@Nonnull
	public Optional<Boolean[]> getBooleanArray(@Nonnull L lookup);

	@Nonnull
	public Optional<Integer[]> getIntArray(@Nonnull L lookup);

	@Nonnull
	public Optional<Float[]> getFloatArray(@Nonnull L lookup);

	@Nonnull
	public Optional<Double[]> getDoubleArray(@Nonnull L lookup);

	@Nonnull
	public Optional<Long[]> getLongArray(@Nonnull L lookup);

	@Nonnull
	public PropertyType getType(@Nonnull L lookup);
}
