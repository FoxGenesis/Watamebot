package net.foxgenesis.watame.property;

import net.foxgenesis.property.Property;

import net.dv8tion.jda.api.entities.Guild;

public interface PluginProperty extends ImmutablePluginProperty, Property<Guild, PluginPropertyMapping> {}
