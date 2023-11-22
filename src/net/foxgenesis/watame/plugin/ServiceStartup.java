package net.foxgenesis.watame.plugin;

import net.foxgenesis.watame.plugin.require.CommandProvider;

/**
 * Abstract class defining common methods for the startup of a service.
 *
 * @author Ashley
 */
public abstract class ServiceStartup {
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
	 * @see #postInit()
	 * @see #onReady()
	 */
	protected abstract void preInit() throws SeverePluginException;

	/**
	 * Startup method called when methods providing functionality are to be loaded.
	 * Methods that require connection the database should be called here.
	 * <p>
	 * <b>Discord information might not be loaded at the time of this method!</b>
	 * Use {@link #onReady()} for functionality that requires valid connections.
	 * </p>
	 *
	 * <p>
	 * Typical methods to call here include:
	 * </p>
	 * <ul>
	 * <li>Main chunk of program initialization</li>
	 * <li>{@link net.foxgenesis.watame.property.PluginProperty PluginProperty}
	 * registration/retrieval</li>
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
	 * @see #postInit()
	 * @see #onReady()
	 */
	protected abstract void init(IEventStore builder) throws SeverePluginException;

	/**
	 * Startup method called when {@link net.dv8tion.jda.api.JDA JDA} is building a
	 * connection to discord and all
	 * {@link net.dv8tion.jda.api.interactions.commands.build.CommandData
	 * CommandData} is being collected from {@link CommandProvider#getCommands()}.
	 * <p>
	 * <b>Discord information might not be loaded at the time of this method!</b>
	 * Use {@link #onReady()} for functionality that requires valid connections.
	 * </p>
	 * <p>
	 * Typical methods to call here include:
	 * </p>
	 * <ul>
	 * <li>Initialization cleanup</li>
	 * </ul>
	 *
	 * @throws SeverePluginException Thrown if the plugin has encountered a
	 *                               <em>severe</em> exception. If the exception is
	 *                               <em>fatal</em>, the plugin will be unloaded
	 */
	protected abstract void postInit() throws SeverePluginException;

	/**
	 * Called by the {@link PluginHandler} when {@link net.dv8tion.jda.api.JDA JDA}
	 * and all {@link Plugin Plugins} have finished startup and have finished
	 * loading.
	 *
	 * @throws SeverePluginException Thrown if the plugin has encountered a
	 *                               <em>severe</em> exception. If the exception is
	 *                               <em>fatal</em>, the plugin will be unloaded
	 *
	 * @see #preInit()
	 * @see #init(IEventStore)
	 * @see #postInit()
	 */
	protected abstract void onReady() throws SeverePluginException;

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
}
