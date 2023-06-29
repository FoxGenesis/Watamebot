package net.foxgenesis.executor;

import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

import org.jetbrains.annotations.NotNull;

public class PrefixedForkJoinPoolFactory implements ForkJoinWorkerThreadFactory {
	@NotNull
	private final String prefix;

	public PrefixedForkJoinPoolFactory(@NotNull String prefix) {
		this.prefix = Objects.requireNonNull(prefix);
	}

	@Override
	public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
		final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
		worker.setName(prefix + '-' + worker.getPoolIndex());
		return worker;
	}

	@NotNull
	public String getPrefix() {
		return prefix;
	}
}
