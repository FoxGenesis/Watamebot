package net.foxgenesis.config.fields;

import org.json.JSONArray;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.watame.sql.IDatabaseManager;


public class ArrayField extends ConfigField<JSONArray> {

	public ArrayField(ConfigKey<JSONArray> key, IDatabaseManager database) {
		super(key, database);
	}

	@Override
	void set(JSONObjectAdv config, JSONArray newState) {
		config.put(getName(), newState);
	}

	@Override
	JSONArray optFrom(JSONObjectAdv config, Guild guild) {
		return config.optJSONArray(getName());
	}

	@Override
	JSONArray from(JSONObjectAdv config) {
		return config.getJSONArray(getName());
	}
}
