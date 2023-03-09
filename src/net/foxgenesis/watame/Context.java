package net.foxgenesis.watame;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.foxgenesis.database.DatabaseManager;
import net.foxgenesis.watame.WatameBot.State;
import net.foxgenesis.watame.plugin.EventStore;

public class Context implements IContext {

	@Nonnull
	private final WatameBot bot;

	@Nonnull
	private final EventStore eventStore;

	public Context(@Nonnull WatameBot bot, @Nonnull JDABuilder builder) {
		this.bot = Objects.requireNonNull(bot);
		this.eventStore = new EventStore(builder);
	}

	@Override
	public DatabaseManager getDatabaseManager() { return (DatabaseManager) bot.getDatabaseManager(); }

	@Override
	public EventStore getEventRegister() { return eventStore; }

	@Override
	public JDA getJDA() { return bot.getJDA(); }

	public State getState() { return bot.getState(); }
	
	public void onJDABuilder(JDA jda) {
		eventStore.setJDA(jda);
	}
}
