package net.foxgenesis.watame.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor.Version;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;

import net.foxgenesis.database.AbstractDatabase;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.util.resource.ConfigType;
import net.foxgenesis.util.resource.ModuleResource;
import net.foxgenesis.util.resource.ResourceUtils;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.require.CommandProvider;
import net.foxgenesis.watame.plugin.require.PluginConfiguration;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyProvider;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service providing functionality to {@link WatameBot}.
 * <p>
 * Providers should provide a <b>no-argument constructor</b> or a
 * {@code public static Plugin provider()} method in accordance to
 * {@link java.util.ServiceLoader.Provider#get() Provider.get()}
 * </p>
 *
 * @author Ashley
 *
 */
public abstract class Plugin extends ServiceStartup {

	/**
	 * Plugin logger
	 */
	@NotNull
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Plugin configurations
	 */
	@NotNull
	private final HashMap<String, Configuration> configs = new HashMap<>();

	/**
	 * Information about the plugin
	 */
	@NotNull
	private final PluginInformation info;

	/**
	 * No-argument constructor called by the {@link java.util.ServiceLoader
	 * ServiceLoader} to load and initialize required plugin data.
	 * <p>
	 * Types of plugin data would include:
	 * </p>
	 * <ul>
	 * <li>Configuration settings</li>
	 * <li>Constants</li>
	 * </ul>
	 * Anything beyond the previous should be loaded in the {@link #preInit()}
	 * method.
	 *
	 * @throws SeverePluginException if the plugin is not in a named module or there
	 *                               was a problem while loading the
	 *                               {@code plugin.properties} file
	 */
	public Plugin() throws SeverePluginException {
		Class<? extends Plugin> c = getClass();
		Module module = c.getModule();

		// Load plugin properties
		Properties properties = new Properties();
		try (InputStream stream = module.getResourceAsStream("/plugin.properties")) {
			properties.load(stream);
		} catch (IOException e) {
			throw new SeverePluginException(e, true);
		}

		// Parse properties file
		info = parseInfo(properties, this);

		// Load configurations if they are present
		if (c.isAnnotationPresent(PluginConfiguration.class)) {
			PluginConfiguration[] configDeclares = c.getDeclaredAnnotationsByType(PluginConfiguration.class);

			for (PluginConfiguration pluginConfig : configDeclares) {

				String id = pluginConfig.identifier();

				// FIXME: sanitize PluginConfiguration IDs

				// Skip over duplicate identifiers
				if (configs.containsKey(id))
					continue;

				try {
					ConfigType type = pluginConfig.type();
					ModuleResource defaults = new ModuleResource(module, pluginConfig.defaultFile());

					// Parse the configuration
					logger.debug("Loading {} configuration for {}", type.name(), pluginConfig.outputFile());
					Configuration config = ResourceUtils.loadConfiguration(type, defaults, info.getConfigurationPath(),
							pluginConfig.outputFile());

					configs.put(id, config);
				} catch (IOException | ConfigurationException e) {
					throw new SeverePluginException(e, false);
				}
			}
		}
	}

	// =========================================================================================================

	/**
	 * Get a list of all loaded configuration {@code ID}s.
	 *
	 * @return Returns a {@link java.util.Set Set} containing the {@code ID}s of all
	 *         configurations loaded
	 */
	protected Set<String> configurationKeySet() {
		return configs.keySet();
	}

	/**
	 * Loop through all loaded {@link Configuration} files.
	 *
	 * @param consumer - {@link PluginConfiguration#identifier()} and the
	 *                 constructed {@link Configuration}
	 */
	protected void forEachConfiguration(BiConsumer<String, Configuration> consumer) {
		configs.entrySet().forEach(e -> consumer.accept(e.getKey(), e.getValue()));
	}

	/**
	 * Check if a configuration file with the specified {@code identifier} exists.
	 *
	 * @param identifier - the {@link PluginConfiguration#identifier()}
	 *
	 * @return Returns {@code true} if the specified {@code identifier} points to a
	 *         valid configuration
	 */
	protected boolean hasConfiguration(String identifier) {
		return configs.containsKey(identifier);
	}

	/**
	 * Get the configuration file that is linked to an {@code identifier}.
	 *
	 * @param identifier - the {@link PluginConfiguration#identifier()}
	 *
	 * @return Returns the {@link PropertiesConfiguration} linked to the
	 *         {@code identifier}
	 * 
	 * @throws NoSuchElementException Thrown if the configuration with the specified
	 *                                {@code identifier} does not exist
	 */
	@NotNull
	protected Configuration getConfiguration(String identifier) {
		if (configs.containsKey(identifier))
			return configs.get(identifier);
		throw new NoSuchElementException();
	}

	/**
	 * Register a plugin property inside the database.
	 *
	 * @param name       - name of the property
	 * @param modifiable - if this property can be modified by a user
	 * @param type       - property storage type
	 *
	 * @return Returns the {@link PropertyInfo} of the registered property
	 */
	protected final PropertyInfo registerProperty(@NotNull String name, boolean modifiable,
			@NotNull PropertyType type) {
		return getPropertyProvider().registerProperty(this, name, modifiable, type);
	}

	/**
	 * Register a plugin property if it doesn't exist and resolve it into a
	 * {@link PluginProperty}.
	 *
	 * @param name       - property name
	 * @param modifiable - if this property can be modified by a user
	 * @param type       - property storage type
	 *
	 * @return Returns the resolved {@link PluginProperty}
	 */
	protected final PluginProperty upsertProperty(@NotNull String name, boolean modifiable,
			@NotNull PropertyType type) {
		return getPropertyProvider().upsertProperty(this, name, modifiable, type);
	}

	/**
	 * Resolve a {@link PropertyInfo} into a usable {@link PluginProperty}.
	 *
	 * @param info - property information
	 *
	 * @return Returns the resolved {@link PluginProperty}
	 */
	protected final PluginProperty getProperty(PropertyInfo info) {
		return getPropertyProvider().getProperty(info);
	}

	/**
	 * Resolve the {@link PropertyInfo} by the specified {@code name} into a usable
	 * {@link PluginProperty}.
	 *
	 * @param name - property name
	 *
	 * @return Returns the resolved {@link PluginProperty}
	 */
	protected final PluginProperty getProperty(String name) {
		return getPropertyProvider().getProperty(this, name);
	}

	/**
	 * Get the provider for registering and getting {@link PluginProperty
	 * PluginProperties}.
	 *
	 * @return Returns the {@link PluginPropertyProvider} used by {@link Plugin
	 *         Plugins} to register plugin properties
	 */
	protected final PluginPropertyProvider getPropertyProvider() {
		return WatameBot.getPropertyProvider();
	}

	/**
	 * Register an {@link AbstractDatabase} that this {@link Plugin} requires.
	 *
	 * @param database - database to register
	 *
	 * @throws IOException Thrown if there was an error while reading the database
	 *                     setup files
	 */
	protected final void registerDatabase(AbstractDatabase database) throws IOException {
		WatameBot.getDatabaseManager().register(this, database);
	}

	// =========================================================================================================

	/**
	 * Get the information about this plugin.
	 *
	 * @return Returns the {@link PluginInformation}
	 */
	public final PluginInformation getInfo() {
		return info;
	}

	@Override
	public String toString() {
		return "Plugin [info=" + info + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(info.getID(), info.getVersion());
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		Plugin other = (Plugin) obj;
		return Objects.equals(info.getID(), other.info.getID())
				&& Objects.equals(info.getVersion(), other.info.getVersion());
	}

	// =========================================================================================================

	/**
	 * Parse the {@link Properties} file of a {@link Plugin}.
	 *
	 * @param properties - plugin.properties file
	 * @param instance   - instance of the plugin
	 *
	 * @return Returns the parsed {@link PluginInformation}
	 */
	private static PluginInformation parseInfo(Properties properties, Plugin instance) {
		String id = Objects.requireNonNull(properties.getProperty("name"), "name must not be null!").trim();

		// FIXME: sanitize plugin IDs

		String friendlyName = Objects.requireNonNull(properties.getProperty("friendlyName"),
				"friendlyName must not be null!");

		Version version = Version
				.parse(Objects.requireNonNull(properties.getProperty("version"), "version must not be null!"));
		String description = properties.getProperty("description", "No description provided");
		boolean providesCommands = instance instanceof CommandProvider;
		boolean needsDatabase = properties.getProperty("needsDatabase", "false").equalsIgnoreCase("true");

		Path configurationPath = WatameBot.CONFIG_PATH.resolve(id);

		return new PluginInformation(id, friendlyName, version, description, providesCommands, needsDatabase,
				configurationPath);
	}
}
