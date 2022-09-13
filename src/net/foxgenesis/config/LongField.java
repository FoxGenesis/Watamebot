package net.foxgenesis.config;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.watame.sql.DatabaseHandler;

public class LongField extends ConfigField<Long> {

	public LongField(ConfigKey<Long> key, DatabaseHandler database) {
		super(key, database);
	}

	@Override
	Long optFrom(JSONObjectAdv config, Guild guild) {
		return config.optLong(getName(), getDefaultValue(guild));
	}

	@Override
	Long from(JSONObjectAdv config) {
		return config.getLong(getName());
	}

	@Override
	void set(JSONObjectAdv config, Long newState) {
		config.put(getName(), (long)newState);
	}
}
