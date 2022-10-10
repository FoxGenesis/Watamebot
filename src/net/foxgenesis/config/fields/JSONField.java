package net.foxgenesis.config.fields;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.watame.sql.IDatabaseManager;

public class JSONField extends ConfigField<JSONObject> {

	public JSONField(ConfigKey<JSONObject> key, IDatabaseManager database) {
		super(key, database);
	}

	@Override
	JSONObject optFrom(JSONObjectAdv config, Guild guild) {
		return config.optJSONObject(getName());
	}

	@Override
	JSONObject from(JSONObjectAdv config) {
		return config.getJSONObject(getName());
	}

	@Override
	void set(JSONObjectAdv config, JSONObject newState) {
		config.put(getName(), newState);
	}
}
