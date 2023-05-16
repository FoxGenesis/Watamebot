package net.foxgenesis.executor;

import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;

import javax.annotation.Nonnull;

import java.util.concurrent.ForkJoinWorkerThread;

public class PrefixedForkJoinPoolFactory implements ForkJoinWorkerThreadFactory {
	@Nonnull
	private final String prefix;

	public PrefixedForkJoinPoolFactory(@Nonnull String prefix) {
		this.prefix = Objects.requireNonNull(prefix);
	}

	@Override
	public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
		final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
		worker.setName(prefix + '-' + worker.getPoolIndex());
		return worker;
	}

	@Nonnull
	public String getPrefix() {
		return prefix;
	}
}
