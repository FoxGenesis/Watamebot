package net.foxgenesis.config.fields;

import java.util.function.Function;

import net.dv8tion.jda.api.entities.Guild;
@Deprecated(forRemoval = true)
public class BooleanField extends ConfigField<Boolean> {

	public BooleanField(String name, Function<Guild, Boolean> defaultValue, boolean isEditable) {
		super(name, defaultValue, isEditable);
	}

	@Override
	Boolean optFrom(JSONObjectAdv config, Guild guild) {
		return isPresent(config) ? config.optBoolean(name) : getDefaultValue(guild);
	}

	@Override
	Boolean from(JSONObjectAdv config, Guild guild) { return config.getBoolean(name); }

	@Override
	void set(JSONObjectAdv config, Boolean newState) { config.put(name, newState); }

	public boolean toggle(Guild guild) { return toggle(getDataForGuild(guild), guild); }

	private boolean toggle(JSONObjectAdv config, Guild guild) {
		boolean l = !optFrom(config, guild);
		set(config, l);
		return l;
	}
}
