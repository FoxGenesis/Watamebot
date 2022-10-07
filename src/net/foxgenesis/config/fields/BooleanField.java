package net.foxgenesis.config.fields;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.watame.sql.IDatabaseManager;

public class BooleanField extends ConfigField<Boolean> {
	
	public BooleanField(ConfigKey<Boolean> key, IDatabaseManager database) {
		super(key, database);
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
		return toggle(getDataForGuild(guild),guild);
	}
	
	private boolean toggle(JSONObjectAdv config, Guild guild) {
		boolean l = !optFrom(config,guild);
		set(config,l);
		return l;
	}
}
