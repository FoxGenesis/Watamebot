package net.foxgenesis.config.fields;

import java.util.function.Function;

import javax.annotation.CheckForNull;

import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;

public class ChannelField extends ConfigField<Channel> {

	public ChannelField(String name, Function<Guild, Channel> defaultValue, boolean isEditable) {
		super(name, defaultValue, isEditable);
	}

	@Override
	@CheckForNull
	Channel optFrom(JSONObjectAdv config, Guild guild) {
		long id = config.optLong(name);
		if (id == 0)
			return getDefaultValue(guild);
		return id == 0 ? getDefaultValue(guild) : guild.getTextChannelById(id);
	}

	@Override
	@CheckForNull
	Channel from(JSONObjectAdv config, Guild guild) { return guild.getTextChannelById(config.getLong(name)); }

	@Override
	void set(JSONObjectAdv config, Channel newState) { config.put(name, newState.getIdLong()); }
}
