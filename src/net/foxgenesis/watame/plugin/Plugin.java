package net.foxgenesis.watame.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor.Version;
import java.nio.file.Path;
import java.util.HashMap;
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
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyProvider;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

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
public abstract class Plugin {

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
	 * Path to the plugin's configuration folder
	 */
	@NotNull
	public final Path configurationPath;

	/**
	 * Name identifier of the plugin.
	 */
	@NotNull
	public final String name;

	/**
	 * Friendly identifier of the plugin.
	 */
	@NotNull
	public final String friendlyName;

	/**
	 * Description of the plugin.
	 */
	@NotNull
	public final String description;

	/**
	 * Version of the plugin.
	 */
	@NotNull
	public final Version version;

	/**
	 * Does this plugin provide commands.
	 */
	public final boolean providesCommands;

	/**
	 * Does this plugin require access to the database.
	 */
	public final boolean needsDatabase;

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

		if (!module.isNamed())
			throw new SeverePluginException("Plugin is not in a named module!");

		// Load plugin properties
		Properties properties = new Properties();
		try (InputStream stream = module.getResourceAsStream("/plugin.properties")) {
			properties.load(stream);

			this.name = Objects.requireNonNull(properties.getProperty("name"), "name must not be null!");
			this.friendlyName = Objects.requireNonNull(properties.getProperty("friendlyName"),
					"friendlyName must not be null!");

			this.version = Version
					.parse(Objects.requireNonNull(properties.getProperty("version"), "version must not be null!"));
			this.description = properties.getProperty("description", "No description provided");
			this.providesCommands = this instanceof CommandProvider;
			this.needsDatabase = properties.getProperty("needsDatabase", "false").equalsIgnoreCase("true");

			this.configurationPath = WatameBot.CONFIG_PATH.resolve(this.name);
		} catch (IOException e) {
			throw new SeverePluginException(e, true);
		}

		// Load configurations if they are present
		if (c.isAnnotationPresent(PluginConfiguration.class)) {
			PluginConfiguration[] configDeclares = c.getDeclaredAnnotationsByType(PluginConfiguration.class);

			for (PluginConfiguration pluginConfig : configDeclares) {
				String id = pluginConfig.identifier();
				// Skip over duplicate identifiers
				if (configs.containsKey(id))
					continue;

				try {
					ConfigType type = pluginConfig.type();
					ModuleResource defaults = new ModuleResource(module, pluginConfig.defaultFile());

					// Parse the configuration
					logger.debug("Loading {} configuration for {}", type.name(), pluginConfig.outputFile());
					Configuration config = ResourceUtils.loadConfiguration(type, defaults, this.configurationPath,
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
	 * Get the configuration file that is linked to an {@code identifier} or
	 * {@code null} if not found.
	 * 
	 * @param identifier - the {@link PluginConfiguration#identifier()}
	 * 
	 * @return Returns the {@link PropertiesConfiguration} linked to the
	 *         {@code identifier}
	 */
	@Nullable
	protected Configuration getConfiguration(String identifier) {
		return configs.getOrDefault(identifier, null);
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
		return WatameBot.INSTANCE.getPropertyProvider();
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
		WatameBot.INSTANCE.getDatabaseManager().register(this, database);
	}

	// =========================================================================================================

	/**
	 * Startup method called when resources, needed for functionality
	 * initialization, are to be loaded. Resources that do <b>not</b> require
	 * connection to Discord or the database should be loaded here.
	 * <p>
	 * <b>The database and Discord information might not be loaded at the time of
	 * this method!</b> Use {@link #init(IEventStore)} for functionality that
	 * requires valid connections.
	 * </p>
	 * 
	 * <p>
	 * Typical resources to load here include:
	 * </p>
	 * <ul>
	 * <li>Custom database registration</li>
	 * <li>System Data</li>
	 * <li>Files</li>
	 * <li>Images</li>
	 * </ul>
	 *
	 * @throws SeverePluginException Thrown if the plugin has encountered a
	 *                               <em>severe</em> exception. If the exception is
	 *                               <em>fatal</em>, the plugin will be unloaded
	 * 
	 * @see #init(IEventStore)
	 * @see #postInit(WatameBot)
	 * @see #onReady(WatameBot)
	 */
	protected abstract void preInit() throws SeverePluginException;

	/**
	 * Startup method called when methods providing functionality are to be loaded.
	 * Methods that require connection the database should be called here.
	 * <p>
	 * <b>Discord information might not be loaded at the time of this method!</b>
	 * Use {@link #onReady(WatameBot)} for functionality that requires valid
	 * connections.
	 * </p>
	 * 
	 * <p>
	 * Typical methods to call here include:
	 * </p>
	 * <ul>
	 * <li>Main chunk of program initialization</li>
	 * <li>{@link PluginProperty} registration/retrieval</li>
	 * <li>Event listener registration</li>
	 * <li>Custom database operations</li>
	 * </ul>
	 * 
	 * @param builder - discord event listener register
	 * 
	 * @throws SeverePluginException Thrown if the plugin has encountered a
	 *                               <em>severe</em> exception. If the exception is
	 *                               <em>fatal</em>, the plugin will be unloaded
	 * 
	 * @see #preInit()
	 * @see #postInit(WatameBot)
	 * @see #onReady(WatameBot)
	 */
	protected abstract void init(IEventStore builder) throws SeverePluginException;

	/**
	 * Startup method called when {@link net.dv8tion.jda.api.JDA JDA} is building a
	 * connection to discord and all {@link CommandData} is being collected from
	 * {@link CommandProvider#getCommands()}.
	 * <p>
	 * <b>Discord information might not be loaded at the time of this method!</b>
	 * Use {@link #onReady(WatameBot)} for functionality that requires valid
	 * connections.
	 * </p>
	 * <p>
	 * Typical methods to call here include:
	 * </p>
	 * <ul>
	 * <li>Initialization cleanup</li>
	 * </ul>
	 * 
	 * @param bot - reference of {@link WatameBot}
	 * 
	 * @throws SeverePluginException Thrown if the plugin has encountered a
	 *                               <em>severe</em> exception. If the exception is
	 *                               <em>fatal</em>, the plugin will be unloaded
	 */
	protected abstract void postInit(WatameBot bot) throws SeverePluginException;

	/**
	 * Called by the {@link PluginHandler} when {@link net.dv8tion.jda.api.JDA JDA}
	 * and all {@link Plugin Plugins} have finished startup and have finished
	 * loading.
	 * 
	 * @param bot - reference of {@link WatameBot}
	 * 
	 * @throws SeverePluginException Thrown if the plugin has encountered a
	 *                               <em>severe</em> exception. If the exception is
	 *                               <em>fatal</em>, the plugin will be unloaded
	 * 
	 * @see #preInit()
	 * @see #init(IEventStore)
	 * @see #postInit(WatameBot)
	 */
	protected abstract void onReady(WatameBot bot) throws SeverePluginException;

	/**
	 * Called when the plugin is to be unloaded.
	 * 
	 * <p>
	 * The shutdown sequence runs as followed:
	 * </p>
	 * <ul>
	 * <li>Remove event listeners</li>
	 * <li>{@link Plugin#close()}</li>
	 * <li>Close databases</li>
	 * </ul>
	 * 
	 * @throws Exception Thrown if an underlying exception is thrown during close
	 */
	protected abstract void close() throws Exception;

	// =========================================================================================================

	public String getDisplayInfo() {
		return this.friendlyName + " v" + version;
	}

	@Override
	public String toString() {
		return "Plugin [name=" + name + ", friendlyName=" + friendlyName + ", description=" + description + ", version="
				+ version + ", providesCommands=" + providesCommands + ", needsDatabase=" + needsDatabase
				+ ", configurationPath=" + configurationPath + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, version);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Plugin other = (Plugin) obj;
		return Objects.equals(name, other.name) && Objects.equals(version, other.version);
	}
}
