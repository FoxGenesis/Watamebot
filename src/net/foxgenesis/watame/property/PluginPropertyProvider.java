package net.foxgenesis.watame.property;

import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyProvider;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.watame.plugin.Plugin;

import net.dv8tion.jda.api.entities.Guild;

public interface PluginPropertyProvider extends PropertyProvider<Plugin, String, Guild, PluginPropertyMapping> {
	/**
	 * Register a {@link PropertyInfo} if it does not exist inside the configuration
	 * and return a new {@link PluginProperty} created with the property
	 * information.
	 *
	 * @param plugin     - property owner
	 * @param key        - property name
	 * @param modifiable - if can be modified by the end user
	 * @param type       - property storage type
	 *
	 * @return Returns a {@link PluginProperty} with the specified
	 *         {@link PropertyInfo}
	 */
	@Override
	PluginProperty upsertProperty(Plugin plugin, String key, boolean modifiable,
			PropertyType type);

	/**
	 * Get a {@link PluginProperty} based on the specified property information
	 * {@code id}.
	 *
	 * @param id - {@link PropertyInfo} id
	 *
	 * @return Returns a {@link PluginProperty} using the specified
	 *         {@link PropertyInfo} {@code id}
	 */
	@Override
	PluginProperty getPropertyByID(int id);

	/**
	 * Get a {@link PluginProperty} based on the specified property {@code owner}
	 * and {@code name}.
	 *
	 * @param plugin - property owner
	 * @param key    - property name
	 *
	 * @return Returns a {@link PluginProperty} linked to the {@link PropertyInfo}
	 *         {@code category} and {@code name}
	 */
	@Override
	PluginProperty getProperty(Plugin plugin, String key);

	/**
	 * Get a {@link PluginProperty} linked to the specified {@link PropertyInfo}.
	 *
	 * @param info - property information
	 *
	 * @return Returns a {@link PluginProperty} with the specified {@code info}
	 */
	@Override
	PluginProperty getProperty(PropertyInfo info);

}
