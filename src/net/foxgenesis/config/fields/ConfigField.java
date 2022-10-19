package net.foxgenesis.config.fields;

import java.util.function.Function;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.watame.WatameBot;

public abstract class ConfigField<E> {
	@Nonnull
	public final String name;

	public final boolean isEditable;

	@Nonnull
	private final Function<Guild, E> defaultValue;

	public ConfigField(@Nonnull String name, @Nonnull Function<Guild, E> defaultValue, boolean isEditable) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.isEditable = isEditable;
	}

	public final boolean isPresent(@Nonnull Guild guild) { return isPresent(getDataForGuild(guild)); }

	public E from(@Nonnull Guild guild) { return from(getDataForGuild(guild)); }

	public E optFrom(@Nonnull Guild guild) { return optFrom(getDataForGuild(guild), guild); }

	public void set(@Nonnull Guild g, E newState) {
		if (isEditable)
			set(getDataForGuild(g), newState);
	}

	public void remove(@Nonnull Guild guild) { remove(getDataForGuild(guild)); }

	private void remove(@Nonnull JSONObjectAdv config) { config.remove(name); }

	protected boolean isPresent(@Nonnull JSONObjectAdv config) { return config.has(name); }

	protected E getDefaultValue(@Nonnull Guild guild) { return defaultValue.apply(guild); }

	protected JSONObjectAdv getDataForGuild(@Nonnull Guild guild) {
		return WatameBot.getInstance().getDatabase().getDataForGuild(guild).getConfig();
	}

	abstract E optFrom(@Nonnull JSONObjectAdv config, @Nonnull Guild guild);

	abstract E from(@Nonnull JSONObjectAdv config);

	abstract void set(@Nonnull JSONObjectAdv config, E newState);
}
