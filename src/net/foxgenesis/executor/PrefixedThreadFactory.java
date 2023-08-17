package net.foxgenesis.executor;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.NotNull;

public class PrefixedThreadFactory implements ThreadFactory {
	private final AtomicLong count = new AtomicLong(1);

	@NotNull
	private final String prefix;

	private final boolean daemon;

	public PrefixedThreadFactory(@NotNull String prefix) { this(prefix, true); }

	@SuppressWarnings("null")
	public PrefixedThreadFactory(@NotNull String prefix, boolean daemon) {
		this.prefix = Objects.requireNonNull(prefix);
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(Runnable r) {
		final Thread thread = new Thread(r, prefix + "-Worker " + count.getAndIncrement());
		thread.setDaemon(daemon);
		return thread;
	}

	@NotNull
	public String getPrefix() { return prefix; }

	public boolean isDaemon() { return daemon; }
}
