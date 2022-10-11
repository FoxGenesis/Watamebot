package net.foxgenesis.config.fields;

import java.util.function.Function;

import net.dv8tion.jda.api.entities.Guild;

public class BooleanField extends ConfigField<Boolean> {

	public BooleanField(String name, Function<Guild, Boolean> defaultValue, boolean isEditable) {
		super(name, defaultValue, isEditable);
	}

	@Override
	Boolean optFrom(JSONObjectAdv config, Guild guild) {
		return isPresent(config) ? config.optBoolean(getName()) : getDefaultValue(guild);
	}

	@Override
	Boolean from(JSONObjectAdv config) {
		return config.getBoolean(getName());
	}

	@Override
	void set(JSONObjectAdv config, Boolean newState) {
		config.put(getName(), newState);
	}

	public boolean toggle(Guild guild) {
		return toggle(getDataForGuild(guild), guild);
	}

	private boolean toggle(JSONObjectAdv config, Guild guild) {
		boolean l = !optFrom(config, guild);
		set(config, l);
		return l;
	}
}
