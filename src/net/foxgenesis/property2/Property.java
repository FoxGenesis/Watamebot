package net.foxgenesis.property2;

import javax.annotation.Nonnull;

public interface Property<L> extends ImmutableProperty<L> {

	public boolean putString(@Nonnull L lookup, @Nonnull String value);

	public boolean putBoolean(@Nonnull L lookup, boolean value);

	public boolean putInt(@Nonnull L lookup, int value);

	public boolean putFloat(@Nonnull L lookup, float value);

	public boolean putDouble(@Nonnull L lookup, double value);

	public boolean putLong(@Nonnull L lookup, long value);

	public boolean putStringArray(@Nonnull L lookup, @Nonnull String[] arr);

	public boolean putBooleanArray(@Nonnull L lookup, @Nonnull boolean[] arr);

	public boolean putIntArray(@Nonnull L lookup, @Nonnull int[] arr);

	public boolean putFloatArray(@Nonnull L lookup, @Nonnull float[] arr);

	public boolean putDoubleArray(@Nonnull L lookup, @Nonnull double[] arr);

	public boolean putLongArray(@Nonnull L lookup, @Nonnull long[] arr);

	public boolean remove(@Nonnull L lookup);
}
