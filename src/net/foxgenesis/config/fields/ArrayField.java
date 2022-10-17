package net.foxgenesis.config.fields;

import java.util.function.Function;

import org.json.JSONArray;

import net.dv8tion.jda.api.entities.Guild;

public class ArrayField extends ConfigField<JSONArray> {

	public ArrayField(String name, Function<Guild, JSONArray> defaultValue, boolean isEditable) {
		super(name, defaultValue, isEditable);
	}

	@Override
	void set(JSONObjectAdv config, JSONArray newState) {
		config.put(name, newState);
	}

	@Override
	JSONArray optFrom(JSONObjectAdv config, Guild guild) {
		return config.optJSONArray(name);
	}

	@Override
	JSONArray from(JSONObjectAdv config) {
		return config.getJSONArray(name);
	}
}
