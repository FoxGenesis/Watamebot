package net.foxgenesis.util;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class CompletableFutureUtils {
	public static CompletableFuture<Void> allOf(Collection<CompletableFuture<?>> coll) {
		return allOf(coll.stream());
	}

	public static CompletableFuture<Void> allOf(Stream<CompletableFuture<?>> stream) {
		return CompletableFuture.allOf(stream.toArray(CompletableFuture[]::new));
	}
}
