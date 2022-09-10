package net.foxgenesis.watame.functionality;

import net.foxgenesis.watame.WatameBot;

/**
 * Interface containing bot functionality
 * startup methods
 * @author Ashley
 *
 */
public interface IStartup {

	/**
	 * Startup method called when resources, needed for
	 * functionality {@linkplain #init(WatameBot) initialization}, 
	 * are to be loaded. Resources that do not require connection
	 * to Discord or the database should be loaded here. <p>
	 * <b>Database and Discord information might not be loaded
	 * at the time of this method!</b> Use {@link #init(WatameBot)}
	 * for functionality that requires valid connections.</p><p>
	 * Typical resources to load here include:<ul>
	 * <li>SQL compiled statements</li>
	 * <li>System Data</li>
	 * <li>Files</li>
	 * <li>Images</li>
	 * </ul></p>
	 * @param watame - the {@link WatameBot} instance
	 * to work with
	 * @see #init(WatameBot)
	 * @see #postInit(WatameBot)
	 */
	public default void preInit(WatameBot watame) {}
	
	/**
	 * NEED_JAVADOC
	 */
	public void init();
	
	/**
	 * NEED_JAVADOC
	 */
	public default void postInit() {}
}
