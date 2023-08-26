package net.foxgenesis.watame.plugin;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.foxgenesis.util.CompletableFutureUtils;
import net.foxgenesis.util.MethodTimer;
import net.foxgenesis.watame.Context;
import net.foxgenesis.watame.plugin.require.CommandProvider;
import net.foxgenesis.watame.plugin.require.RequiresCache;
import net.foxgenesis.watame.plugin.require.RequiresIntents;
import net.foxgenesis.watame.plugin.require.RequiresMemberCachePolicy;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

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
	private final CopyOnWriteArraySet<T> plugins = new CopyOnWriteArraySet<>();

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
	 * @param context     - instance context
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
	public void loadPlugins() {
		logger.info("Checking for plugins...");
		long time = System.nanoTime();

		Collection<Provider<T>> providers = getProviders();

		logger.info("Found {} plugins", providers.size());
		logger.info("Constructing plugins...");

		plugins.addAll(construct(providers));
		plugins.forEach(context.getEventRegister()::register);

		time = System.nanoTime() - time;
		logger.info("Constructed all plugins in {}ms", MethodTimer.formatToMilli(time));
	}

	/**
	 * Get all {@link Provider Providers} of {@code T} from the
	 * {@link ServiceLoader}. This method will only take <b>one</b> {@code T} per
	 * {@link Module}.
	 *
	 * @return Returns a {@link Collection} of {@link Provider Providers}
	 */
	private Collection<Provider<@NotNull T>> getProviders() {
		Map<Module, Provider<T>> providers = new HashMap<>();
		for (Provider<T> provider : loader.stream().toList()) {
			Module module = provider.type().getModule();

			// Check if module is already present
			if (providers.containsKey(module)) {
				logger.warn("A plugin is already registered for module: {}! Skipping...", module);
				continue;
			}

			providers.put(module, provider);
		}

		return providers.values();
	}

	private Collection<@NotNull T> construct(Collection<Provider<T>> providers) {
		// Copy list
		List<Provider<T>> list = new ArrayList<>(providers);
		Set<Class<? extends T>> classes = new HashSet<>();

		// Filter out duplicate plugin classes
		Iterator<Provider<@NotNull T>> i = list.iterator();
		while (i.hasNext()) {
			Provider<T> provider = i.next();
			Class<? extends T> c = provider.type();

			// Check if plugin class is already registered
			if (classes.contains(c)) {
				logger.error("Plugin class {}(Provider: {}) has already been provided by another plugin! Skipping...",
						c, provider);
				i.remove();
				continue;
			}

			// Register plugin class
			classes.add(c);
		}

		// Construct all plugins
		return list.parallelStream().mapMulti((Provider<T> provider, Consumer<T> consumer) -> {
			Class<? extends T> c = provider.type();

			try {
				// Call plugin constructor
				logger.debug("Loading {}", c);
				T plugin = provider.get();
				logger.info("Loaded {}", plugin.getInfo().getDisplayInfo());
				consumer.accept(plugin);
			} catch (Exception e) {
				logger.error("Error while constructing " + c, e);
			}
		}).toList();
	}

	/**
	 * Pre-Initialize all plugins.
	 */
	public void preInit() {
		forEachPlugin(Plugin::preInit, null).join();
	}

	/**
	 * Initialize all plugins.
	 */
	@NotNull
	public void init() {
		forEachPlugin(plugin -> plugin.init(context.getEventRegister()), null).join();
	}

	/**
	 * Post-Initialize all plugins.
	 */
	@NotNull
	public void postInit() {
		forEachPlugin(Plugin::postInit, null).join();
	}

	/**
	 * Post-Initialize all plugins.
	 *
	 * @return Returns a {@link CompletableFuture} that completes when all plugins
	 *         have finished their {@link Plugin#onReady()}
	 */
	@NotNull
	public CompletableFuture<Void> onReady() {
		return forEachPlugin(Plugin::onReady, null);
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
		plugins.stream().filter(p -> p instanceof CommandProvider).map(CommandProvider.class::cast)
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
		return CompletableFutureUtils.allOf(plugins.stream().filter(filter).map(plugin -> CompletableFuture
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
		plugins.remove(plugin);
		context.getEventRegister().unregister(plugin);
		try {
			plugin.close();
		} catch (Exception e) {
			pluginError(plugin, new SeverePluginException(e, false));
		}
		if (plugin.getInfo().requiresDatabase())
			context.getDatabaseManager().unload(plugin);
		logger.warn(plugin.getInfo().getDisplayInfo() + " unloaded");
	}

	/**
	 * Indicate that a plugin has thrown an error during one of its initialization
	 * methods.
	 *
	 * @param plugin - plugin in question
	 * @param error  - the error that was thrown
	 */
	private void pluginError(T plugin, Throwable error) {
		Throwable temp = error;

		if (error instanceof CompletionException && error.getCause() instanceof SeverePluginException)
			temp = error.getCause();

		String header = "";
		if (temp instanceof SeverePluginException pluginException) {
			Marker m = MarkerFactory.getMarker(pluginException.isFatal() ? "FATAL" : "SEVERE");

			header = "Exception in " + plugin.getInfo().getDisplayName();
			logger.error(m, header, pluginException);

			if (pluginException.isFatal())
				unloadPlugin(plugin);
		} else {
			header = "Error in " + plugin.getInfo().getDisplayName();
			logger.error(header, temp);
		}

		context.pushNotification("An Error Occurred in Watame", header + "\n\n" + ExceptionUtils.getStackTrace(temp));
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
	 * Get all {@link GatewayIntent}s required for all {@code plugins} to operate
	 * normally.
	 *
	 * @return Returns an {@link EnumSet} of all required {@link GatewayIntent}s
	 */
	public EnumSet<GatewayIntent> getGatewayIntents() {
		return collectEnums(plugins, this::getRequiredIntents, GatewayIntent.class);
	}

	/**
	 * Get all {@link CacheFlag}s required for all {@code plugins} to operate
	 * normally.
	 *
	 * @return Returns an {@link EnumSet} of all required {@link CacheFlag}s
	 */
	public EnumSet<CacheFlag> getCaches() {
		return collectEnums(plugins, this::getRequiredCaches, CacheFlag.class);
	}

	/**
	 * Get the {@link MemberCachePolicy} required by all plugins.
	 * 
	 * @return Returns a {@link MemberCachePolicy} created by combining all declared
	 *         policies into
	 *         {@link MemberCachePolicy#any(MemberCachePolicy, MemberCachePolicy...)}
	 */
	public MemberCachePolicy getRequiredCachePolicy() {
		MemberCachePolicy[] list = plugins.stream()
				// Check if plugin declared a policy
				.filter(p -> p instanceof RequiresMemberCachePolicy)
				// Cast to policy provider
				.map(p -> (RequiresMemberCachePolicy) p)
				// Get declared policy
				.map(RequiresMemberCachePolicy::getPolicy)
				// Only use non null policies
				.filter(Objects::nonNull)
				// Collect into an array
				.toArray(MemberCachePolicy[]::new);
		return MemberCachePolicy.any(MemberCachePolicy.NONE, list);
	}

	/**
	 * Get the required {@link GatewayIntent}s for the specified {@code plugin}.
	 *
	 * @param plugin - plugin to check
	 *
	 * @return Returns an {@link EnumSet} of all {@link GatewayIntent}s required for
	 *         normal operation of the {@code plugin}
	 */
	@SuppressWarnings({ "null" })
	public EnumSet<CacheFlag> getRequiredCaches(T plugin) {
		if (plugin instanceof RequiresCache r)
			return r.getRequiredCaches();
		return EnumSet.noneOf(CacheFlag.class);
	}

	/**
	 * Get the required {@link GatewayIntent}s for the specified {@code plugin}.
	 *
	 * @param plugin - plugin to check
	 *
	 * @return Returns an {@link EnumSet} of all {@link GatewayIntent}s required for
	 *         normal operation of the {@code plugin}
	 */
	@SuppressWarnings("null")
	public EnumSet<GatewayIntent> getRequiredIntents(T plugin) {
		if (plugin instanceof RequiresIntents r)
			return r.getRequiredIntents();
		return EnumSet.noneOf(GatewayIntent.class);
	}

	/**
	 * Check if a plugin is loaded.
	 *
	 * @param identifier - plugin identifier
	 *
	 * @return Returns {@code true} if the plugin is loaded
	 */
	public boolean isPluginPresent(String identifier) {
		return getPlugin(identifier) != null;
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
	 * Check if a plugin from the specified module is loaded.
	 *
	 * @param module - module to check with
	 *
	 * @return Returns {@code true} if the plugin is loaded. {@code false} otherwise
	 */
	public boolean isPluginPresent(Module module) {
		return getPluginForModule(module) != null;
	}

	/**
	 * Get the plugin specified by the {@code identifier}.
	 *
	 * @param identifier - plugin identifier
	 *
	 * @return Returns the {@link Plugin} with the specified {@code identifier}
	 */
	@Nullable
	public T getPlugin(String identifier) {
		String temp = identifier.trim().toLowerCase();
		return filterPlugins(p -> p.getInfo().getID().equalsIgnoreCase(temp));
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
		return filterPlugins(p -> pluginClass.isInstance(p));
	}

	/**
	 * Get the plugin of a module
	 *
	 * @param module - module containing the plugin
	 *
	 * @return Returns the found {@link Plugin} if present
	 */
	@Nullable
	public T getPluginForModule(Module module) {
		return filterPlugins(p -> p.getClass().getModule().equals(module));
	}

	/**
	 * Find a plugin based on a {@code filter}.
	 *
	 * @param filter - filter to use
	 *
	 * @return Returns the first plugin found by the specified {@code filter}.
	 *         Otherwise {@code null}.
	 */
	@Nullable
	private T filterPlugins(Predicate<T> filter) {
		for (T plugin : plugins)
			if (filter.test(plugin))
				return plugin;
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
	 * @return Returns the thread pool used for asynchronous execution
	 */
	@NotNull
	public ExecutorService getAsynchronousExecutor() {
		return pluginExecutor;
	}

	/**
	 * Collect all enumerations from a {@link Collection} of objects.
	 *
	 * @param <P>
	 *
	 * @param <E>       Enumeration type
	 * @param coll      - collection of objects
	 * @param func      - function to grab enumerations from a plugin
	 * @param enumClass - class of enumeration
	 *
	 * @return Returns a {@link EnumSet} of all enumerations found in the specified
	 *         collection
	 */
	private static <P, E extends Enum<E>> EnumSet<@Nullable E> collectEnums(Collection<P> coll,
			Function<P, EnumSet<E>> func, Class<E> enumClass) {
		EnumSet<E> set = EnumSet.noneOf(enumClass);
		for (P plugin : coll)
			set.addAll(func.apply(plugin));
		return set;
	}
}
