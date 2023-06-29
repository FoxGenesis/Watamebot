package net.foxgenesis.property2.impl;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import net.foxgenesis.property2.PropertyType;
import net.foxgenesis.property2.async.AsyncProperty;
import net.foxgenesis.property2.async.AsyncPropertyResolver;

public class AsyncPropertyImpl<L> extends PropertyImpl<L, AsyncPropertyResolver<L>> implements AsyncProperty<L> {

	public AsyncPropertyImpl(@NotNull String key, @NotNull AsyncPropertyResolver<L> resolver) {
		super(key, resolver);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<String>> getStringAsync(@NotNull L lookup) {
		return resolver.getStringAsync(lookup, key);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Boolean>> getBooleanAsync(@NotNull L lookup) {
		return resolver.getBooleanAsync(lookup, key);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Integer>> getIntAsync(@NotNull L lookup) {
		return resolver.getIntAsync(lookup, key);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Float>> getFloatAsync(@NotNull L lookup) {
		return resolver.getFloatAsync(lookup, key);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Double>> getDoubleAsync(@NotNull L lookup) {
		return resolver.getDoubleAsync(lookup, key);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Long>> getLongAsync(@NotNull L lookup) {
		return resolver.getLongAsync(lookup, key);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<String[]>> getStringArrayAsync(@NotNull L lookup) {
		return resolver.getStringArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@NotNull L lookup) {
		return resolver.getBooleanArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@NotNull L lookup) {
		return resolver.getIntArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@NotNull L lookup) {
		return resolver.getFloatArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@NotNull L lookup) {
		return resolver.getDoubleArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public CompletableFuture<Optional<Long[]>> getLongArrayAsync(@NotNull L lookup) {
		return resolver.getLongArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putStringAsync(@NotNull L lookup, String value) {
		return resolver.putStringAsync(lookup, value, value);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putBooleanAsync(@NotNull L lookup, boolean value) {
		return resolver.putBooleanAsync(lookup, key, value);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putIntAsync(@NotNull L lookup, int value) {
		return resolver.putIntAsync(lookup, key, value);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putFloatAsync(@NotNull L lookup, float value) {
		return resolver.putFloatAsync(lookup, key, value);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putDoubleAsync(@NotNull L lookup, double value) {
		return resolver.putDoubleAsync(lookup, key, value);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putLongAsync(@NotNull L lookup, long value) {
		return resolver.putLongAsync(lookup, key, value);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putStringArrayAsync(@NotNull L lookup, @NotNull String[] arr) {
		return resolver.putStringArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putBooleanArrayAsync(@NotNull L lookup, boolean[] arr) {
		return resolver.putBooleanArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putIntArrayAsync(@NotNull L lookup, int[] arr) {
		return resolver.putIntArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putFloatArrayAsync(@NotNull L lookup, float[] arr) {
		return resolver.putFloatArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putDoubleArrayAsync(@NotNull L lookup, double[] arr) {
		return resolver.putDoubleArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> putLongArrayAsync(@NotNull L lookup, long[] arr) {
		return resolver.putLongArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@NotNull
	public CompletableFuture<Boolean> removeAsync(@NotNull L lookup) {
		return resolver.removeAsync(lookup, key);
	}

	@Override
	@NotNull
	public CompletableFuture<PropertyType> getTypeAsync(@NotNull L lookup) {
		return resolver.typeOfAsync(lookup, key);
	}
}
