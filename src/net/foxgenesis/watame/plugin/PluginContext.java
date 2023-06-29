package net.foxgenesis.watame.plugin;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.property.IGuildPropertyProvider;

public class PluginContext {

	@NotNull
	private final WatameBot bot;

	PluginContext(WatameBot bot) {
		this.bot = Objects.requireNonNull(bot);
	}

	public IGuildPropertyProvider getPropertyProvider() {
		return bot.getPropertyProvider();
	}
}
