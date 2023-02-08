package net.foxgenesis.watame.plugin;

import java.util.Collection;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UntrustedPluginLoader<T> {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(UntrustedPluginLoader.class);

	/**
	 * Plugin class type
	 */
	@Nonnull
	private final Class<T> pluginClass;

	@Nonnull
	private final ServiceLoader<T> serviceLoader;

	/**
	 * Create a new {@link UntrustedPluginLoader} instance using the provided
	 * service class.
	 *
	 * @param pluginClass - service class used to load plugins
	 * 
	 * @throws NullPointerException if {@code pluginClass} is {@code null}
	 */
	public UntrustedPluginLoader(@Nonnull Class<T> pluginClass) {
		this.pluginClass = Objects.requireNonNull(pluginClass);

		logger.trace("Creating logger of service {}", pluginClass);
		serviceLoader = ServiceLoader.load(pluginClass);
	}

	/**
	 * Get all plugins of type {@link T} in the selected folder.
	 * <p>
	 * This method is effectively equivalent to: <blockquote>
	 *
	 * <pre>
	 * getPlugins(Provider::get)
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @return A {@link Collection} of {@link T}
	 * @throws ServiceConfigurationError if the service type is not accessible to
	 *                                   the caller or the caller is in an explicit
	 *                                   module and its module descriptor does not
	 *                                   declare that it uses service
	 * @see #getPlugins(Function)
	 */
	public Collection<T> getPlugins() { return getPlugins(Provider::get); }

	/**
	 * Get all plugins of type {@link T} in the selected folder.
	 *
	 * @param providerMap - {@link Function} to map a {@link Provider} to {@link T}
	 * @return A {@link Collection} of {@link T}
	 * @throws ServiceConfigurationError if the service type is not accessible to
	 *                                   the caller or the caller is in an explicit
	 *                                   module and its module descriptor does not
	 *                                   declare that it uses service
	 * @see #getPlugins()
	 */
	public Collection<T> getPlugins(@Nonnull Function<Provider<T>, T> providerMap) {
		Objects.requireNonNull(providerMap, "Mapper must not be null");
		
		logger.trace("Searching for providers of {}", pluginClass);
		return serviceLoader.stream().map(providerMap).toList();
	}

	/**
	 * Clear this loader's provider cache so that all providers will be reloaded. 
	 */
	public void reload() {
		serviceLoader.reload();
	}
	
	/**
	 * Get the class of {@link T}.
	 *
	 * @return
	 */
	@Nonnull
	public Class<T> getPluginClass() { return pluginClass; }
}
