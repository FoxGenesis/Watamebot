package net.foxgenesis.config.fields;

import java.util.function.Function;

import net.dv8tion.jda.api.entities.Guild;
@Deprecated(forRemoval = true)
public class LongField extends ConfigField<Long> {

	public LongField(String name, Function<Guild, Long> defaultValue, boolean isEditable) {
		super(name, defaultValue, isEditable);
	}

	@Override
	Long optFrom(JSONObjectAdv config, Guild guild) { return config.optLong(name, getDefaultValue(guild)); }

	@Override
	Long from(JSONObjectAdv config, Guild guild) { return config.getLong(name); }

	@Override
	void set(JSONObjectAdv config, Long newState) { config.put(name, (long) newState); }
}
