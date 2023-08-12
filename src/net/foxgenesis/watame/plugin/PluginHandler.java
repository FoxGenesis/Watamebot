package net.foxgenesis.watame.plugin;

import java.io.Closeable;
import java.util.List;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.foxgenesis.util.CompletableFutureUtils;
import net.foxgenesis.util.MethodTimer;
import net.foxgenesis.watame.Context;
import net.foxgenesis.watame.WatameBot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

/**
 * Class used to handle all plugin related tasks.
 * 
 * @author Ashley
 *
 * @param <T> - the plugin class this instance uses
 */
public class PluginHandler<@NotNull T extends Plugin> implements Closeable {
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(PluginHandler.class);

	/**
	 * Map of plugins
	 */
	@NotNull
	private final ConcurrentHashMap<String, T> plugins = new ConcurrentHashMap<>();

	/**
	 * Service loader to load plugins
	 */
	@NotNull
	private final ServiceLoader<T> loader;

	/**
	 * Module layer of the loader
	 */
	@NotNull
	private final ModuleLayer layer;

	/**
	 * Class of the plugin we are loading
	 */
	@NotNull
	private final Class<T> pluginClass;

	/**
	 * Thread pool for loading plugins
	 */
	@NotNull
	private final ExecutorService pluginExecutor;

	/**
	 * Startup asynchronous executor
	 */
	@NotNull
	private final Context context;

	/**
	 * Construct a new {@link PluginHandler} with the specified {@link ModuleLayer}
	 * and plugin {@link Class}.
	 * 
	 * @param layer       - layer the {@link ServiceLoader} should use
	 * @param pluginClass - the plugin {@link Class} to load
	 */
	public PluginHandler(Context context, ModuleLayer layer, Class<T> pluginClass) {
		this.context = Objects.requireNonNull(context);
		this.layer = Objects.requireNonNull(layer);
		this.pluginClass = Objects.requireNonNull(pluginClass);

		loader = ServiceLoader.load(layer, pluginClass);
		pluginExecutor = context.getAsynchronousExecutor();
	}

	/**
	 * Load all plugins from the service loader
	 */
	@SuppressWarnings("resource")
	public void loadPlugins() {
		logger.info("Checking for plugins...");
		List<Provider<T>> providers = loader.stream().toList();

		logger.info("Found {} plugins", providers.size());
		logger.info("Constructing plugins...");

		long time = System.nanoTime();

		providers.forEach(provider -> {
			logger.debug("Loading {}", provider.type());

			try {
				T plugin = provider.get();
				plugins.put(plugin.name, plugin);
				context.getEventRegister().register(plugin);

				logger.info("Loaded {}", plugin.getDisplayInfo());
			} catch (ServiceConfigurationError e) {
				logger.error("Failed to load " + provider.type(), e);
			}
		});

		time = System.nanoTime() - time;
		logger.info("Constructed all plugins in {}ms", MethodTimer.formatToMilli(time));
	}

	/**
	 * Pre-Initialize all plugins.
	 * 
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#preInit()}
	 */
	@NotNull
	public CompletableFuture<Void> preInit() {
		return forEachPlugin(Plugin::preInit, null);
	}

	/**
	 * Initialize all plugins.
	 * 
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#init(IEventStore)}
	 */
	@NotNull
	public CompletableFuture<Void> init() {
		return forEachPlugin(plugin -> plugin.init(context.getEventRegister()), null);
	}

	/**
	 * Post-Initialize all plugins.
	 * 
	 * @param watamebot - reference to {@link WatameBot} that is passed on to the
	 *                  plugin's {@code postInit}
	 * 
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#postInit(WatameBot)}
	 */
	@NotNull
	public CompletableFuture<Void> postInit(WatameBot watamebot) {
		return forEachPlugin(plugin -> plugin.postInit(watamebot), null);
	}

	/**
	 * Post-Initialize all plugins.
	 * 
	 * @param watamebot - reference to {@link WatameBot} that is passed on to the
	 *                  plugin's {@code onReady}o
	 * 
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#onReady(WatameBot)}
	 */
	@NotNull
	public CompletableFuture<Void> onReady(WatameBot watamebot) {
		return forEachPlugin(plugin -> plugin.onReady(watamebot), null);
	}

	/**
	 * Fill a {@link CommandListUpdateAction} will all commands specified by the
	 * loaded plugins.
	 * 
	 * @param action - update task to fill
	 * 
	 * @return Returns the action for chaining
	 */
	@NotNull
	public CommandListUpdateAction updateCommands(CommandListUpdateAction action) {
		plugins.values().stream().filter(p -> p instanceof CommandProvider).map(CommandProvider.class::cast)
				.map(CommandProvider::getCommands).filter(Objects::nonNull).forEach(action::addCommands);
		return action;
	}

	/**
	 * Iterate over all plugins that match the {@code filter} and perform a task.
	 * Additionally, any plugin that fires a <b>fatal</b>
	 * {@link SeverePluginException} will be unloaded.
	 * 
	 * @param task   - task that is executed for every plugin in the filter
	 * @param filter - filter to select what plugins to use or {@code null} for all
	 *               plugins
	 * 
	 * @return Returns a {@link CompletableFuture} that completes after all plugins
	 *         have finished the {@code task}.
	 */
	@NotNull
	private CompletableFuture<Void> forEachPlugin(Consumer<? super T> task, @Nullable Predicate<Plugin> filter) {
		if (filter == null)
			filter = p -> true;
		return CompletableFutureUtils.allOf(plugins.values().stream().filter(filter).map(plugin -> CompletableFuture
				.runAsync(() -> task.accept(plugin), pluginExecutor).exceptionallyAsync(error -> {
					pluginError(plugin, error);
					return null;
				}, pluginExecutor)));
	}

	/**
	 * Remove a plugin from the managed plugins, closing its resources in the
	 * process.
	 * 
	 * @param plugin - the plugin to unload
	 */
	@SuppressWarnings("resource")
	private void unloadPlugin(T plugin) {
		logger.debug("Unloading {}", plugin.getClass());
		plugins.remove(plugin.name);
		context.getEventRegister().unregister(plugin);
		try {
			plugin.close();
		} catch (Exception e) {
			pluginError(plugin, new SeverePluginException(e, false));
		}
		if (plugin.needsDatabase) {
			logger.info("Unloading database connections from ", plugin.getDisplayInfo());
			context.getDatabaseManager().unload(plugin);
		}
		logger.warn(plugin.getDisplayInfo() + " unloaded");
	}

	/**
	 * Indicate that a plugin has thrown an error during one of its initialization
	 * methods.
	 * 
	 * @param plugin - plugin in question
	 * @param error  - the error that was thrown
	 * @param marker - method marker
	 */
	private void pluginError(T plugin, Throwable error) {
		Throwable temp = error;

		if (error instanceof CompletionException && error.getCause() instanceof SeverePluginException)
			temp = error.getCause();

		if (temp instanceof SeverePluginException) {
			SeverePluginException pluginException = (SeverePluginException) temp;

			Marker m = MarkerFactory.getMarker(pluginException.isFatal() ? "FATAL" : "SEVERE");

			logger.error(m, "Exception in " + plugin.friendlyName, pluginException);

			if (pluginException.isFatal())
				unloadPlugin(plugin);
		} else
			logger.error("Error in " + plugin.friendlyName, error);
	}

	/**
	 * Close all loaded plugins and <b>wait</b> for the termination of the plugin
	 * thread pool.
	 */
	@Override
	public void close() {
		logger.debug("Closing all pugins");
		forEachPlugin(this::unloadPlugin, null);
	}

	/**
	 * Check if a plugin is loaded.
	 * 
	 * @param identifier - plugin identifier
	 * 
	 * @return Returns {@code true} if the plugin is loaded
	 */
	public boolean isPluginPresent(String identifier) {
		return plugins.containsKey(identifier);
	}

	/**
	 * Check if a plugin is loaded.
	 * 
	 * @param pluginClass - class of the plugin to check
	 * 
	 * @return Returns {@code true} if the specified plugin was found
	 */
	public boolean isPluginPresent(Class<? extends T> pluginClass) {
		return getPlugin(pluginClass) != null;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param identifier
	 * 
	 * @return
	 */
	@Nullable
	public T getPlugin(String identifier) {
		return plugins.get(identifier);
	}

	/**
	 * Get a plugin by class.
	 * 
	 * @param pluginClass - plugin class
	 * 
	 * @return Returns the found {@link Plugin} if found, otherwise {@code null}
	 */
	@Nullable
	public T getPlugin(Class<? extends T> pluginClass) {
		for (T p : plugins.values())
			if (pluginClass.isInstance(p))
				return p;
		return null;
	}

	/**
	 * Get the class used by this instance.
	 * 
	 * @return Returns a {@link Class} that is used by the {@link ServiceLoader} to
	 *         load the plugins
	 */
	@NotNull
	public Class<T> getPluginClass() {
		return pluginClass;
	}

	/**
	 * Get the module layer used by this instance.
	 * 
	 * @return Returns a {@link ModuleLayer} that is used by the
	 *         {@link ServiceLoader} to load the plugins
	 */
	@NotNull
	public ModuleLayer getModuleLayer() {
		return layer;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	@NotNull
	public ExecutorService getAsynchronousExecutor() {
		return pluginExecutor;
	}
}
