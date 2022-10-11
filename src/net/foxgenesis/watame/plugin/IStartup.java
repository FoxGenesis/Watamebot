package net.foxgenesis.watame.plugin;

import net.foxgenesis.watame.WatameBot;

/**
 * Interface containing bot functionality startup methods
 * 
 * @author Ashley
 *
 */
public interface IStartup {
	
	public void construct(PluginProperties properties);

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
	 * @see #init()
	 * @see #postInit()
	 */
	public void preInit(WatameBot bot);

	/**
	 * NEED_JAVADOC
	 */
	public void init(WatameBot bot);

	/**
	 * NEED_JAVADOC
	 */
	public void postInit(WatameBot bot);
}
