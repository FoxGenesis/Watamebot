package net.foxgenesis.config.fields;

import java.util.function.Function;

import net.dv8tion.jda.api.entities.Guild;

public class StringField extends ConfigField<String> {

	public StringField(String name, Function<Guild, String> defaultValue, boolean isEditable) {
		super(name, defaultValue, isEditable);
	}

	@Override
	String optFrom(JSONObjectAdv config, Guild guild) {
		return isPresent(config) ? config.optString(name) : getDefaultValue(guild);
	}

	@Override
	String from(JSONObjectAdv config) { return config.getString(name); }

	@Override
	void set(JSONObjectAdv config, String newState) { config.put(name, newState); }
}
