package net.foxgenesis.property2;

import org.jetbrains.annotations.NotNull;

public interface Property<L> extends ImmutableProperty<L> {

	public boolean putString(@NotNull L lookup, @NotNull String value);

	public boolean putBoolean(@NotNull L lookup, boolean value);

	public boolean putInt(@NotNull L lookup, int value);

	public boolean putFloat(@NotNull L lookup, float value);

	public boolean putDouble(@NotNull L lookup, double value);

	public boolean putLong(@NotNull L lookup, long value);

	public boolean putStringArray(@NotNull L lookup, @NotNull String[] arr);

	public boolean putBooleanArray(@NotNull L lookup, boolean[] arr);

	public boolean putIntArray(@NotNull L lookup, int[] arr);

	public boolean putFloatArray(@NotNull L lookup, float[] arr);

	public boolean putDoubleArray(@NotNull L lookup, double[] arr);

	public boolean putLongArray(@NotNull L lookup, long[] arr);

	public boolean remove(@NotNull L lookup);
}
