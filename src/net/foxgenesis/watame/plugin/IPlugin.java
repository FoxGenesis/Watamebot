package net.foxgenesis.watame.plugin;

import java.io.File;
import java.lang.Runtime.Version;
import java.util.Collection;
import java.util.Collections;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.foxgenesis.watame.ProtectedJDABuilder;
import net.foxgenesis.watame.WatameBot;

/**
 * NEED_JAVADOC
 *
 * @author Ashley
 *
 */
@Deprecated(forRemoval = true)
public interface IPlugin extends AutoCloseable {

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
	public void preInit() throws SeverePluginException;

	/**
	 * NEED_JAVADOC
	 *
	 * @see #preInit()
	 * @see #postInit(WatameBot)
	 */
	public void init(ProtectedJDABuilder builder) throws SeverePluginException;

	/**
	 * NEED_JAVADOC
	 *
	 * @param bot
	 * @see #preInit()
	 * @see #init(ProtectedJDABuilder)
	 */
	public void postInit(WatameBot bot) throws SeverePluginException;

	/**
	 * NEED_JAVADOC
	 *
	 * @param bot
	 */
	public void onReady(WatameBot bot) throws SeverePluginException;
	

	public default Collection<CommandData> getCommands() { return Collections.emptyList(); }

	/**
	 * Get the name of this plugin.
	 *
	 * @return A string containing the name of the plugin
	 * @see #getDescription()
	 * @see #getVersion()
	 */
	public default String getName() { return getProperties(getClass()).name(); }

	/**
	 * Get the description of this plugin.
	 *
	 * @return A string containing the description of this plugin
	 * @see #getVersion()
	 * @see #getName()
	 */
	public default String getDescription() { return getProperties(getClass()).description(); }

	/**
	 * Get the version of this plugin.
	 *
	 * @return A {@link Version} representing this plugin
	 * @see #getName()
	 * @see #getDescription()
	 */
	public default Version getVersion() { return Version.parse(getProperties(getClass()).version()); }

	/**
	 * Check whether this plugin provides commands/interactions.
	 * 
	 * @return If this plugin provides command data
	 */
	public default boolean providesCommands() { return getProperties(getClass()).providesCommands(); }

	/**
	 * Check whether this plugin requires access to the database connection.
	 * 
	 * @return If this plugin uses the database
	 */
	public default boolean requiresDatabaseConnection() { return getProperties(getClass()).requiresDatabaseAccess(); }

	/**
	 * Get the configuration directory of this plugin. <br>
	 * <br>
	 * NOTE: This file may or may not exist. If you have configuration files that
	 * you would like to deploy or read from, check if it exists / create a new
	 * directory.
	 * 
	 * @return A {@link File} pointing to the directory that is to be used to store
	 *         configuration files for this plugin
	 */
	public default File getPluginConfigurationDirectory() { return new File(WatameBot.CONFIG_DIR + getName()); }

	/**
	 * Get the annotated {@link PluginProperties} of a class
	 *
	 * @param _class - {@link Class} to get from
	 * @return properties of class
	 */
	private static PluginProperties getProperties(Class<? extends IPlugin> _class) {
		return _class.getDeclaredAnnotation(PluginProperties.class);
	}
}
