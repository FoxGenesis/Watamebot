package net.foxgenesis.watame.property;

import java.util.List;

import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.watame.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

public interface PluginPropertyProvider {
	PropertyInfo registerProperty(@NotNull Plugin plugin, @NotNull String key, boolean modifiable, @NotNull PropertyType type);
	
	PluginProperty upsertProperty(@NotNull Plugin plugin, @NotNull String key, boolean modifiable, @NotNull PropertyType type);
	
	PluginProperty getPropertyInfoByID(int id);

	PluginProperty getProperty(@NotNull Plugin plugin, @NotNull String key);

	PluginProperty getProperty(@NotNull PropertyInfo info);
	
	boolean propertyExists(@NotNull Plugin plugin, @NotNull String key);

	List<PropertyInfo> getPropertyList();
}
