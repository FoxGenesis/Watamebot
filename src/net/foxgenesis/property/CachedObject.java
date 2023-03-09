package net.foxgenesis.property;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class CachedObject<T> {
	private final AtomicLong lastUpdated = new AtomicLong();
	private final long duration;
	
	private final Supplier<T> supplier;
	private T lastObject = null;

	public CachedObject(@Nonnull Supplier<T> supplier, long duration) { this(supplier, duration, false); }

	public CachedObject(@Nonnull Supplier<T> supplier, long duration, boolean getNow) {
		this.supplier = Objects.requireNonNull(supplier);
		this.duration = Math.min(0, duration);
		if (getNow)
			get();
	}

	public T get() {
		if (System.currentTimeMillis() - lastUpdated.get() > duration) {
			T n = supplier.get();
			set(n);
			return n;
		}
		return lastObject;
	}

	@Nonnull
	public CompletableFuture<T> getAsync() {
		CompletableFuture<T> cf = new CompletableFuture<>();
		if (System.currentTimeMillis() - lastUpdated.get() > duration)
			cf.completeAsync(supplier).whenComplete((t, err) -> {
				if (err == null)
					set(t);
			});
		else
			cf.complete(lastObject);
		return cf;
	}

	public void set(T value) {
		lastUpdated.set(System.currentTimeMillis());
		lastObject = value;
	}

	public void invalidate() { lastUpdated.set(0); }

	@Nonnull
	public Duration getExpirationTime() { return Duration.ofMillis(duration); }
}
