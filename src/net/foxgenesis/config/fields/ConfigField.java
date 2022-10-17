package net.foxgenesis.config.fields;

import java.util.function.Function;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.watame.WatameBot;

public abstract class ConfigField<E> {

	public final String name;
	public final boolean isEditable;
	private final Function<Guild, E> defaultValue;

	public ConfigField(@Nonnull String name, @Nonnull Function<Guild, E> defaultValue, boolean isEditable) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.isEditable = isEditable;
	}

	public final boolean isPresent(Guild guild) {
		return isPresent(getDataForGuild(guild));
	}

	public E from(Guild guild) {
		return from(getDataForGuild(guild));
	}

	public E optFrom(Guild guild) {
		return optFrom(getDataForGuild(guild), guild);
	}

	public void set(Guild g, E newState) {
		if (isEditable())
			set(getDataForGuild(g), newState);
	}

	public void remove(Guild guild) {
		remove(getDataForGuild(guild));
	}

	private void remove(JSONObjectAdv config) {
		config.remove(name);
	}

	protected boolean isPresent(JSONObjectAdv config) {
		return config.has(name);
	}

	protected E getDefaultValue(Guild guild) {
		return defaultValue.apply(guild);
	}

	protected JSONObjectAdv getDataForGuild(Guild guild) {
		return WatameBot.getInstance().getDatabase().getDataForGuild(guild).getConfig();
	}

	abstract E optFrom(JSONObjectAdv config, Guild guild);

	abstract E from(JSONObjectAdv config);

	abstract void set(JSONObjectAdv config, E newState);
}
