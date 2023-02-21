package net.foxgenesis.watame;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.JDA;
import net.foxgenesis.database.DatabaseManager;
import net.foxgenesis.watame.WatameBot.State;

public class Context implements IContext {

	@Nonnull
	private final WatameBot bot;

	public Context(@Nonnull WatameBot bot) { this.bot = Objects.requireNonNull(bot); }

	@Override
	public DatabaseManager getDatabaseManager() { return (DatabaseManager) bot.getDatabaseManager(); }

	@Override
	public JDA getJDA() { return bot.getJDA(); }

	public State getState() { return bot.getState(); }
}
