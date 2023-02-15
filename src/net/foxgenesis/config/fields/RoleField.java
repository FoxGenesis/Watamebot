package net.foxgenesis.config.fields;

import java.util.function.Function;

import javax.annotation.CheckForNull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
@Deprecated(forRemoval = true)
public class RoleField extends ConfigField<Role> {

	public RoleField(String name, Function<Guild, Role> defaultValue, boolean isEditable) {
		super(name, defaultValue, isEditable);
	}

	@Override
	@CheckForNull
	Role optFrom(JSONObjectAdv config, Guild guild) {
		long id = config.optLong(name);
		if (id == 0)
			return getDefaultValue(guild);
		return id == 0 ? getDefaultValue(guild) : guild.getRoleById(id);
	}

	@Override
	@CheckForNull
	Role from(JSONObjectAdv config, Guild guild) { return guild.getRoleById(config.getLong(name)); }

	@Override
	void set(JSONObjectAdv config, Role newState) { config.put(name, newState.getIdLong()); }
}
