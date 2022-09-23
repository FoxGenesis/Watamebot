package net.foxgenesis.config;

import java.util.function.Function;

import net.dv8tion.jda.api.entities.Guild;

public class ConfigKey<E> {
	public final String name;
	public final boolean isEditable;
	public final Function<Guild, E> defaultValue;
	
	public ConfigKey(String name, Function<Guild, E> defaultValue, boolean isEditable) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.isEditable = isEditable;
	}
}
