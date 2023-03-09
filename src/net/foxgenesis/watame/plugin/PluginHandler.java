package net.foxgenesis.watame.plugin;

import java.io.IOException;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.foxgenesis.util.CompletableFutureUtils;
import net.foxgenesis.watame.Context;
import net.foxgenesis.watame.ProtectedJDABuilder;
import net.foxgenesis.watame.WatameBot;

/**
 * Class used to handle all plugin related tasks.
 * 
 * @author Ashley
 *
 * @param <T> - the plugin class this instance uses
 */
public class PluginHandler<T extends Plugin> implements AutoCloseable {
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(PluginHandler.class);

	/**
	 * Map of plugins
	 */
	private final ConcurrentHashMap<String, T> plugins = new ConcurrentHashMap<>();

	/**
	 * Service loader to load plugins
	 */
	@Nonnull
	private final ServiceLoader<T> loader;

	/**
	 * Module layer of the loader
	 */
	@Nonnull
	private final ModuleLayer layer;

	/**
	 * Class of the plugin we are loading
	 */
	@Nonnull
	private final Class<T> pluginClass;

	/**
	 * Thread pool for loading plugins
	 */
	@Nonnull
	private final ForkJoinPool pluginExecutor;

	@Nonnull
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

		pluginExecutor = ForkJoinPool.commonPool();
	}

	/**
	 * Load all plugins from the service loader
	 */
	@SuppressWarnings("resource")
	public void loadPlugins() {
		logger.info("Starting...");
		loader.stream().map(provider -> provider.get()).forEach(plugin -> {
			logger.info("Loading {}", plugin.getDisplayInfo());
			plugins.put(plugin.name, plugin);
			context.getEventRegister().register(plugin);
		});
		logger.debug("Found {} plugins", plugins.size());
	}

	/**
	 * Pre-Initialize all plugins.
	 * 
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#preInit()}
	 */
	@Nonnull
	public CompletableFuture<Void> preInit() {
		logger.debug("Calling plugin pre-initialization async");
		return forEachPlugin(Plugin::preInit, null);
	}

	/**
	 * Initialize all plugins.
	 * 
	 * @param builder - Protected {@link JDABuilder} used to add event listeners
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#init(ProtectedJDABuilder)}
	 */
	@Nonnull
	public CompletableFuture<Void> init() {
		logger.debug("Calling plugin initialization async");
		return forEachPlugin(plugin -> plugin.init(context.getEventRegister()), null);
	}

	/**
	 * Post-Initialize all plugins.
	 * 
	 * @param watamebot - reference to {@link WatameBot} that is passed on to the
	 *                  plugin's {@code postInit}
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#postInit(WatameBot)}
	 */
	@Nonnull
	public CompletableFuture<Void> postInit(WatameBot watamebot) {
		logger.debug("Calling plugin post-initialization async");
		return forEachPlugin(plugin -> plugin.postInit(watamebot), null);
	}

	/**
	 * Post-Initialize all plugins.
	 * 
	 * @param watamebot - reference to {@link WatameBot} that is passed on to the
	 *                  plugin's {@code onReady}o
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#onReady(WatameBot)}
	 */
	@Nonnull
	public CompletableFuture<Void> onReady(WatameBot watamebot) {
		logger.debug("Calling plugin on ready async");
		return forEachPlugin(plugin -> plugin.onReady(watamebot), null);
	}

	/**
	 * Fill a {@link CommandListUpdateAction} will all commands specified by the
	 * loaded plugins.
	 * 
	 * @param action - update task to fill
	 * @return Returns the action for chaining
	 */
	@Nonnull
	public CommandListUpdateAction updateCommands(CommandListUpdateAction action) {
		plugins.values().stream().filter(p -> p.providesCommands).map(Plugin::getCommands).forEach(action::addCommands);
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
	 * @return Returns a {@link CompletableFuture} that completes after all plugins
	 *         have finished the {@code task}.
	 */
	@Nonnull
	private CompletableFuture<Void> forEachPlugin(Consumer<? super T> task, @Nullable Predicate<Plugin> filter) {
		if (filter == null)
			filter = p -> true;
		return CompletableFutureUtils.allOf(plugins.values().stream().filter(filter).map(
				plugin -> CompletableFuture.runAsync(() -> task.accept(plugin), pluginExecutor).exceptionally(error -> {
					pluginError(plugin, error);
					return null;
				})));
	}

	/**
	 * Remove a plugin from the managed plugins, closing its resources in the
	 * process.
	 * 
	 * @param plugin - the plugin to unload
	 */
	@SuppressWarnings("resource")
	private void unloadPlugin(T plugin) {
		logger.trace("Unloading {}", plugin.getClass());
		plugins.remove(plugin.name);
		context.getEventRegister().unregister(plugin);
		IOUtil.silentClose(plugin);
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
		MDC.put("watame.status", context.getState().name());
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
	 * Close all loaded plugins and <b>await/help</b> the termination of the plugin
	 * thread pool.
	 */
	@Override
	public void close() throws IOException {
		loader.reload();

		logger.debug("Closing all pugins");
		plugins.values().stream().filter(plugin -> plugin.needsDatabase)
				.forEach(plugin -> context.getDatabaseManager().unload(plugin));
		plugins.values().forEach(plugin -> IOUtil.silentClose(plugin));

		pluginExecutor.shutdown();

		// Await all plugin futures to complete
		if (!pluginExecutor.awaitQuiescence(30, TimeUnit.SECONDS)) {
			logger.warn("Timed out waiting for plugin pool shutdown. Continuing shutdown...");
			pluginExecutor.shutdownNow();
		}

		plugins.clear();
	}

	/**
	 * Check if a plugin is loaded.
	 * 
	 * @param identifier - plugin identifier
	 * @return Returns {@code true} if the plugin is loaded
	 */
	public boolean isPluginPresent(String identifier) { return plugins.containsKey(identifier); }

	public Plugin getPlugin(String identifier) { return plugins.get(identifier); }

	/**
	 * Get the class used by this instance.
	 * 
	 * @return Returns a {@link Class} that is used by the {@link ServiceLoader} to
	 *         load the plugins
	 */
	@Nonnull
	public Class<T> getPluginClass() { return pluginClass; }

	/**
	 * Get the module layer used by this instance.
	 * 
	 * @return Returns a {@link ModuleLayer} that is used by the
	 *         {@link ServiceLoader} to load the plugins
	 */
	@Nonnull
	public ModuleLayer getModuleLayer() { return layer; }
}
