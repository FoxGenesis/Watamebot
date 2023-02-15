package net.foxgenesis.executor;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

public class PrefixedThreadFactory implements ThreadFactory {
	private final AtomicLong count = new AtomicLong(1);

	@Nonnull
	private final String prefix;
	
	private final boolean daemon;

	public PrefixedThreadFactory(@Nonnull String prefix) { this(prefix, true); }

	public PrefixedThreadFactory(@Nonnull String prefix, boolean daemon) {
		this.prefix = Objects.requireNonNull(prefix);
		this.daemon = daemon;
	}

	public Thread newThread(Runnable r) {
		final Thread thread = new Thread(r, prefix + "-Worker " + count.getAndIncrement());
		thread.setDaemon(daemon);
		return thread;
	}

	@Nonnull
	public String getPrefix() { return prefix; }

	public boolean isDaemon() { return daemon; }
}
