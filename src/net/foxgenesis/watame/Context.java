package net.foxgenesis.watame;

import java.io.Closeable;
import java.lang.module.ModuleDescriptor.Version;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.foxgenesis.database.DatabaseManager;
import net.foxgenesis.watame.WatameBot.State;
import net.foxgenesis.watame.plugin.EventStore;

public class Context implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(Context.class);

	private final WatameBot bot;

	private final EventStore eventStore;

	private final ExecutorService executor;

	private final Version version;

	public Context(@NotNull WatameBot bot, @NotNull JDABuilder builder, @Nullable ExecutorService executor,
			@NotNull Version version) {
		this.bot = Objects.requireNonNull(bot);
		this.executor = Objects.requireNonNullElse(executor, ForkJoinPool.commonPool());
		this.eventStore = new EventStore(builder);
		this.version = Objects.requireNonNull(version);
	}

	@NotNull
	public DatabaseManager getDatabaseManager() {
		return (DatabaseManager) bot.getDatabaseManager();
	}

	@NotNull
	public EventStore getEventRegister() {
		return eventStore;
	}

	@NotNull
	public ExecutorService getAsynchronousExecutor() {
		return executor;
	}

	@Nullable
	public JDA getJDA() {
		return bot.getJDA();
	}

	@NotNull
	public State getState() {
		return bot.getState();
	}

	@NotNull
	public Version getVersion() {
		return version;
	}

	void onJDABuilder(JDA jda) {
		eventStore.setJDA(jda);
	}

	@Override
	public void close() {
		// The common pool does not need to be manually shutdown
		if (executor == ForkJoinPool.commonPool())
			return;

		// We are finished with our executor
		executor.shutdown();

		try {
			// Wait for plugin executor to shutdown
			if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
				logger.warn("Timed out waiting for plugin pool shutdown. Continuing shutdown...");
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {}
	}
}
