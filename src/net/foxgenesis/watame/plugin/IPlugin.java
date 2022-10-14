package net.foxgenesis.watame.plugin;

import java.lang.Runtime.Version;
import java.util.Properties;

import net.dv8tion.jda.api.JDA;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.WatameBot.ProtectedJDABuilder;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
public interface IPlugin {

	/**
	 * Construction method used to register event listeners if they require to start
	 * listening to events while {@link JDA} is still building. For all other event
	 * listener registration, use {@link #preInit()} or {@link #init(WatameBot)}.
	 * 
	 * @param builder - wrapper class of {@link JDABuilder} containing event
	 *                listener registration
	 * @see #preInit()
	 * @see #init(WatameBot)
	 */
	public default void _construct(ProtectedJDABuilder builder) {}

	/**
	 * Startup method called when resources, needed for functionality
	 * {@linkplain #init(WatameBot) initialization}, are to be loaded. Resources
	 * that do not require connection to Discord or the database should be loaded
	 * here.
	 * <p>
	 * <b>Database and Discord information might not be loaded at the time of this
	 * method!</b> Use {@link #init(WatameBot)} for functionality that requires
	 * valid connections.
	 * </p>
	 * <p>
	 * Typical resources to load here include:
	 * <ul>
	 * <li>SQL compiled statements</li>
	 * <li>System Data</li>
	 * <li>Files</li>
	 * <li>Images</li>
	 * </ul>
	 * </p>
	 * 
	 * @see #_construct(ProtectedJDABuilder)
	 * @see #init(WatameBot)
	 * @see #postInit(WatameBot)
	 */
	public void preInit();

	/**
	 * NEED_JAVADOC
	 * 
	 * @param bot
	 * @see #preInit()
	 * @see #postInit(WatameBot)
	 */
	public void init(WatameBot bot);

	/**
	 * NEED_JAVADOC
	 * 
	 * @param bot
	 * @see #preInit()
	 * @see #init(WatameBot)
	 */
	public void postInit(WatameBot bot);

	/**
	 * Get the name of this plugin.
	 * 
	 * @return A string containing the name of the plugin
	 */
	public default String getName() {
		return getProperties().getProperty("name", "Untitled Plugin");
	}

	/**
	 * Get the description of this plugin.
	 * 
	 * @return A string containing the description of this plugin
	 * @see #getVersion()
	 * @see #getProperties()
	 */
	public default String getDescription() {
		return getProperties().getProperty("description", "No description");
	}

	/**
	 * Get the version of this plugin.
	 * 
	 * @return A {@link Version} representing this plugin
	 * @see #getName()
	 * @see #getProperties()
	 */
	public default Version getVersion() {
		return Version.parse(getProperties().getProperty("version", "0.0.0"));
	}

	/**
	 * Get the properties file contained in the plugin
	 * 
	 * @return A read-only {@link Properties} file
	 * @see #getName()
	 * @see #getVersion()
	 */
	public PluginProperties getProperties();
}
