package net.foxgenesis.config.fields;

import java.util.function.Function;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;

public class JSONField extends ConfigField<JSONObject> {

	public JSONField(String name, Function<Guild, JSONObject> defaultValue, boolean isEditable) {
		super(name, defaultValue, isEditable);
	}

	@Override
	JSONObject optFrom(JSONObjectAdv config, Guild guild) {
		return config.optJSONObject(name);
	}

	@Override
	JSONObject from(JSONObjectAdv config) {
		return config.getJSONObject(name);
	}

	@Override
	void set(JSONObjectAdv config, JSONObject newState) {
		config.put(name, newState);
	}
}
