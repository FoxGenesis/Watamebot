package net.foxgenesis.property.lck.impl;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public class CachedObject<T> {
	private final AtomicReference<T> obj = new AtomicReference<>();
	private final AtomicLong lastCache = new AtomicLong();
	private final Supplier<T> updateFunction;

	private final long cacheTime;

	public CachedObject(Supplier<T> updateFunction, long cacheTime) {
		this.updateFunction = Objects.requireNonNull(updateFunction);
		this.cacheTime = cacheTime;
	}

	@Nullable
	public synchronized T get() {
		if (System.currentTimeMillis() - lastCache.get() > cacheTime)
			return getNew();
		return obj.get();
	}

	@Nullable
	public synchronized T getNew() {
		return set(updateFunction.get());
	}

	@Nullable
	public synchronized T set(T newValue) {
		lastCache.set(System.currentTimeMillis());
		obj.set(newValue);
		return newValue;
	}

	public void invalidate() {
		lastCache.set(0);
	}

	public long getCacheTime() {
		return cacheTime;
	}
}
