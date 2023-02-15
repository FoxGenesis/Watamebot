package net.foxgenesis.watame.plugin;

import java.util.Properties;

import org.apache.commons.configuration2.PropertiesConfiguration;

import net.foxgenesis.watame.ProtectedJDABuilder;
import net.foxgenesis.watame.WatameBot;

public abstract class PluginStartup {
	protected abstract void onPropertiesLoaded(Properties properties);

	protected abstract void onConfigurationLoaded(PropertiesConfiguration properties);

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
}
