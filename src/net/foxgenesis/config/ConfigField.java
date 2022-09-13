package net.foxgenesis.config;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.watame.sql.DatabaseHandler;

public abstract class ConfigField<E> {
	
	private final ConfigKey<E> key;
	private final DatabaseHandler database;

	public ConfigField(ConfigKey<E> key, DatabaseHandler database) {
		this.key = key;
		this.database = database;
	}
	
	public final String getName() {
		return key.name;
	}

	public final boolean isEditable() {
		return key.isEditable;
	}

	public final boolean isPresent(Guild guild) {
		return isPresent(getDataForGuild(guild));
	}

	public E from(Guild guild) {
		return from(getDataForGuild(guild));
	}

	public E optFrom(Guild guild) {
		return optFrom(getDataForGuild(guild), guild);
	}

	public void set(Guild g, E newState) {
		if (isEditable())
			set(getDataForGuild(g), newState);
	}

	public void remove(Guild guild) {
		remove(getDataForGuild(guild));
	}
	
	private void remove(JSONObjectAdv config) {
		config.remove(key.name);
	}
	
	protected boolean isPresent(JSONObjectAdv config) {
		return config.has(key.name);
	}
	
	protected E getDefaultValue(Guild guild) {
		return key.defaultValue.apply(guild);
	}
	
	protected JSONObjectAdv getDataForGuild(Guild guild) {
		return database.getDataForGuild(guild);
	}
	
	abstract E optFrom(JSONObjectAdv config, Guild guild);

	abstract E from(JSONObjectAdv config);

	abstract void set(JSONObjectAdv config, E newState);
}
