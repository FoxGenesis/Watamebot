package net.foxgenesis.watame.plugin;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.property.IPropertyProvider;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.property.IGuildPropertyMapping;

public class PluginContext {

	@Nonnull
	private final WatameBot bot;
	
	PluginContext(@Nonnull WatameBot bot) {
		this.bot = Objects.requireNonNull(bot);
	}
	
	public IPropertyProvider<String,Guild,IGuildPropertyMapping> getPropertyProvider() {
		return bot.getPropertyProvider();
	}
	
}
