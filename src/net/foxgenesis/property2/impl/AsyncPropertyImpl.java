package net.foxgenesis.property2.impl;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import net.foxgenesis.property2.PropertyType;
import net.foxgenesis.property2.async.AsyncProperty;
import net.foxgenesis.property2.async.AsyncPropertyResolver;

public class AsyncPropertyImpl<L> extends PropertyImpl<L, AsyncPropertyResolver<L>> implements AsyncProperty<L> {

	public AsyncPropertyImpl(@Nonnull String key, @Nonnull AsyncPropertyResolver<L> resolver) { super(key, resolver); }

	@Override
	@Nonnull
	public CompletableFuture<Optional<String>> getStringAsync(@Nonnull L lookup) {
		return resolver.getStringAsync(lookup, key);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Boolean>> getBooleanAsync(@Nonnull L lookup) {
		return resolver.getBooleanAsync(lookup, key);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Integer>> getIntAsync(@Nonnull L lookup) {
		return resolver.getIntAsync(lookup, key);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Float>> getFloatAsync(@Nonnull L lookup) {
		return resolver.getFloatAsync(lookup, key);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Double>> getDoubleAsync(@Nonnull L lookup) {
		return resolver.getDoubleAsync(lookup, key);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Long>> getLongAsync(@Nonnull L lookup) {
		return resolver.getLongAsync(lookup, key);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<String[]>> getStringArrayAsync(@Nonnull L lookup) {
		return resolver.getStringArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Boolean[]>> getBooleanArrayAsync(@Nonnull L lookup) {
		return resolver.getBooleanArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Integer[]>> getIntArrayAsync(@Nonnull L lookup) {
		return resolver.getIntArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Float[]>> getFloatArrayAsync(@Nonnull L lookup) {
		return resolver.getFloatArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Double[]>> getDoubleArrayAsync(@Nonnull L lookup) {
		return resolver.getDoubleArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@Nonnull
	public CompletableFuture<Optional<Long[]>> getLongArrayAsync(@Nonnull L lookup) {
		return resolver.getLongArrayAsync(lookup, key, DELIMETER);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putStringAsync(@Nonnull L lookup, String value) {
		return resolver.putStringAsync(lookup, value, value);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putBooleanAsync(@Nonnull L lookup, boolean value) {
		return resolver.putBooleanAsync(lookup, key, value);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putIntAsync(@Nonnull L lookup, int value) {
		return resolver.putIntAsync(lookup, key, value);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putFloatAsync(@Nonnull L lookup, float value) {
		return resolver.putFloatAsync(lookup, key, value);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putDoubleAsync(@Nonnull L lookup, double value) {
		return resolver.putDoubleAsync(lookup, key, value);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putLongAsync(@Nonnull L lookup, long value) {
		return resolver.putLongAsync(lookup, key, value);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putStringArrayAsync(@Nonnull L lookup, @Nonnull String[] arr) {
		return resolver.putStringArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putBooleanArrayAsync(@Nonnull L lookup, @Nonnull boolean[] arr) {
		return resolver.putBooleanArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putIntArrayAsync(@Nonnull L lookup, @Nonnull int[] arr) {
		return resolver.putIntArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putFloatArrayAsync(@Nonnull L lookup, @Nonnull float[] arr) {
		return resolver.putFloatArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putDoubleArrayAsync(@Nonnull L lookup, @Nonnull double[] arr) {
		return resolver.putDoubleArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> putLongArrayAsync(@Nonnull L lookup, @Nonnull long[] arr) {
		return resolver.putLongArrayAsync(lookup, key, DELIMETER, arr);
	}

	@Override
	@Nonnull
	public CompletableFuture<Boolean> removeAsync(@Nonnull L lookup) { return resolver.removeAsync(lookup, key); }

	@Override
	@Nonnull
	public CompletableFuture<PropertyType> getTypeAsync(@Nonnull L lookup) { return resolver.typeOfAsync(lookup, key); }
}
