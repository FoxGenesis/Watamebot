package net.foxgenesis.watame;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	public Context(@Nonnull WatameBot bot, @Nonnull JDABuilder builder, @Nonnull ExecutorService executor) {
		this.bot = Objects.requireNonNull(bot);
		this.executor = Objects.requireNonNull(executor);
		this.eventStore = new EventStore(builder);
	}

	@Nonnull
	public DatabaseManager getDatabaseManager() {
		return (DatabaseManager) bot.getDatabaseManager();
	}

	@Nonnull
	public EventStore getEventRegister() {
		return eventStore;
	}

	@Nonnull
	public ExecutorService getAsynchronousExecutor() {
		return executor;
	}

	@Nullable
	public JDA getJDA() {
		return bot.getJDA();
	}

	@Nonnull
	public State getState() {
		return bot.getState();
	}

	void onJDABuilder(JDA jda) {
		eventStore.setJDA(jda);
	}

	@Override
	public void close() {
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
