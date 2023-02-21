package net.foxgenesis.watame.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Runtime.Version;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.util.ResourceUtils.ModuleResource;
import net.foxgenesis.watame.ProtectedJDABuilder;
import net.foxgenesis.watame.WatameBot;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
public abstract class Plugin implements AutoCloseable {
	@Nonnull
	private static final Path CONFIG_PATH = Paths.get("config");

	static {
		try {
			Files.createDirectories(CONFIG_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// =========================================================================================================

	/**
	 * Plugin logger
	 */
	@Nonnull
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * Plugin configurations
	 */
	@Nonnull
	private final HashMap<String, PropertiesConfiguration> configs = new HashMap<>();
	/**
	 * Commands
	 */
	@Nonnull
	private final HashMap<String, CommandData> commands = new HashMap<>();

	/**
	 * Path to the plugin's configuration folder
	 */
	@Nonnull
	public final Path configurationPath;

	/**
	 * Name identifier of the plugin.
	 */
	@Nonnull
	public final String name;
	/**
	 * Friendly identifier of the plugin.
	 */
	@Nonnull
	public final String friendlyName;
	/**
	 * Description of the plugin.
	 */
	@Nonnull
	public final String description;
	/**
	 * Version of the plugin.
	 */
	@Nonnull
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
	 * No-arg constructor to load and initialize required plugin data.
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
		try (InputStream stream = module.getResourceAsStream("/plugin.properties")) {
			Properties properties = new Properties();
			properties.load(stream);

			this.name = Objects.requireNonNull(properties.getProperty("name"), "name must not be null!");
			this.friendlyName = Objects.requireNonNull(properties.getProperty("friendlyName"),
					"friendlyName must not be null!");

			this.version = Runtime.Version
					.parse(Objects.requireNonNull(properties.getProperty("version"), "version must not be null!"));
			this.description = properties.getProperty("description", "No description provided");
			this.providesCommands = properties.getProperty("providesCommands", "false").equalsIgnoreCase("true");
			this.needsDatabase = properties.getProperty("needsDatabase", "false").equalsIgnoreCase("true");

			this.configurationPath = CONFIG_PATH.resolve(this.name);

			// Fire on load event
			CompletableFuture.runAsync(() -> onPropertiesLoaded(properties));
		} catch (Exception e) {
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
					logger.debug("Loading configuration for {}", pluginConfig.outputFile());
					PropertiesConfiguration config = ResourceUtils.loadConfig(
							new ModuleResource(module, pluginConfig.defaultFile()), this.configurationPath,
							pluginConfig.outputFile());

					configs.put(id, config);

					// Fire on load event
					CompletableFuture.runAsync(() -> onConfigurationLoaded(id, config));
				} catch (IOException | ConfigurationException e) {
					throw new SeverePluginException(e, false);
				}
			}
		}
	}

	// =========================================================================================================

	/**
	 * Check if a configuration file with the specified {@code identifier} exists.
	 * 
	 * @param identifier - the {@link PluginConfiguration#identifier()}
	 * @return Returns {@code true} if the specified {@code identifier} points to a
	 *         valid configuration
	 */
	protected boolean hasConfiguration(String identifier) { return configs.containsKey(identifier); }

	/**
	 * Get the configuration file that is linked to an {@code identifier} or
	 * {@code null} if not found.
	 * 
	 * @param identifier - the {@link PluginConfiguration#identifier()}
	 * @return Returns the {@link PropertiesConfiguration} linked to the
	 *         {@code identifier}
	 */
	@Nullable
	protected PropertiesConfiguration getConfiguration(String identifier) {
		return configs.getOrDefault(identifier, null);
	}

	protected boolean hasCommandData(String command) { return commands.containsKey(command); }

	@Nullable
	protected CommandData getCommandData(String command) { return commands.getOrDefault(command, null); }

	@Nonnull
	protected Collection<CommandData> getCommands() { return Collections.unmodifiableCollection(commands.values()); }

	// =========================================================================================================

	protected abstract void onPropertiesLoaded(Properties properties);

	protected abstract void onConfigurationLoaded(String identifier, PropertiesConfiguration properties);

	/**
	 * Startup method called when resources, needed for functionality
	 * initialization, are to be loaded. Resources that do not require connection to
	 * Discord or the database should be loaded here.
	 * <p>
	 * <b>Database and Discord information might not be loaded at the time of this
	 * method!</b> Use {@link #onReady(WatameBot)} for functionality that requires
	 * valid connections.
	 * 
	 * <p>
	 * Typical resources to load here include:
	 * <ul>
	 * <li>SQL compiled statements</li>
	 * <li>System Data</li>
	 * <li>Files</li>
	 * <li>Images</li>
	 * </ul>
	 * 
	 *
	 * @see #init(ProtectedJDABuilder)
	 * @see #postInit(WatameBot)
	 */
	protected abstract void preInit() throws SeverePluginException;

	protected abstract void init(ProtectedJDABuilder builder) throws SeverePluginException;

	protected abstract void postInit(WatameBot bot) throws SeverePluginException;

	protected abstract void onReady(WatameBot bot) throws SeverePluginException;


	// =========================================================================================================

	public String getDisplayInfo() { return this.friendlyName + " v" + version; }

	@Override
	public String toString() {
		return "Plugin [name=" + name + ", friendlyName=" + friendlyName + ", description=" + description + ", version="
				+ version + ", providesCommands=" + providesCommands + ", needsDatabase=" + needsDatabase
				+ ", configurationPath=" + configurationPath + "]";
	}

	@Override
	public int hashCode() { return Objects.hash(name, version); }

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