package net.foxgenesis.watame.plugin;

import java.lang.Runtime.Version;

import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.WatameBot.ProtectedJDABuilder;

/**
 * NEED_JAVADOC
 *
 * @author Ashley
 *
 */
public interface IPlugin extends AutoCloseable {

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
	 * @see #preInit()
	 * @see #postInit(WatameBot)
	 */
	public void init(ProtectedJDABuilder builder);

	/**
	 * NEED_JAVADOC
	 *
	 * @param bot
	 * @see #preInit()
	 * @see #init(WatameBot)
	 */
	public void postInit(WatameBot bot);

	/**
	 * NEED_JAVADOC
	 *
	 * @param bot
	 */
	public void onReady(WatameBot bot);

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
	 * Get the annotated {@link PluginProperties} of a class
	 *
	 * @param _class - {@link Class} to get from
	 * @return properties of class
	 */
	private static PluginProperties getProperties(Class<? extends IPlugin> _class) {
		return _class.getDeclaredAnnotation(PluginProperties.class);
	}
}
