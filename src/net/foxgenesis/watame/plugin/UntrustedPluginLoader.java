package net.foxgenesis.watame.plugin;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.Function;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UntrustedPluginLoader<T> implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(UntrustedPluginLoader.class);

	/**
	 * {@link FileFilter} to select all jar files
	 */
	private static final FileFilter filter = file -> file.getPath().toLowerCase().endsWith(".jar");

	/**
	 * Plugin folder
	 */
	private File folder;

	private URLClassLoader loader;

	@Nonnull
	private final Class<T> pluginClass;

	public UntrustedPluginLoader(@Nonnull Class<T> pluginClass) {
		this(pluginClass, new File("plugins"));
	}

	/**
	 * Create a new {@link PluginLoader} instance and set the directory to use.
	 * <p>
	 * This constructor is effectively equivalent to: <blockquote>
	 * 
	 * <pre>
	 * new PluginLoader(folder, false)
	 * </pre>
	 * 
	 * </blockquote>
	 * </p>
	 * 
	 * @param folder - The {@link File} to search for plugins in.
	 */
	public UntrustedPluginLoader(@Nonnull Class<T> pluginClass, @Nonnull File folder) {
		this(pluginClass, folder, true);
	}

	/**
	 * Create a new {@link PluginLoader} instance and set the directory to use.
	 * 
	 * @param folder - The {@link File} to search for plugins in.
	 * @param mkdirs - If {@code folder} does not exist, should a new one be made
	 */
	public UntrustedPluginLoader(@Nonnull Class<T> pluginClass, @Nonnull File folder, boolean mkdirs) {
		Objects.nonNull(pluginClass);
		this.pluginClass = pluginClass;

		setPluginDirectory(folder, mkdirs);
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
	 * </p>
	 * 
	 * @return A {@link Collection} of {@link T}
	 * @throws ServiceConfigurationError if the service type is not accessible to
	 *                                   the caller or the caller is in an explicit
	 *                                   module and its module descriptor does not
	 *                                   declare that it uses service
	 * @see #getPlugins(Function)
	 */
	public Collection<T> getPlugins() {
		return getPlugins(Provider::get);
	}

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
	public Collection<T> getPlugins(Function<Provider<T>, T> providerMap) {
		File[] files = getJarsInFolder(folder);
		if (files == null || files.length == 0) {
			logger.warn("No jar files found in plugins directory");
			return null;
		}

		URL[] urls = filesToURLArray(files);

		loader = createUntrustedClassLoader(urls);

		ServiceLoader<T> sl = ServiceLoader.load(pluginClass, loader);
		return sl.stream().map(providerMap).toList();
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param urls
	 * @return
	 */
	private URLClassLoader createUntrustedClassLoader(@Nonnull URL[] urls) {
		Objects.nonNull(urls);

		try {
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// FIXME add security measures for untrusted code
		URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());

		return loader;
	}

	/**
	 * Set the plugin folder to use during class search.
	 * 
	 * @param folder - The {@link File} to search in
	 * @param mkdirs - If {@code folder} does not exist, should a new directory be
	 *               made
	 * @throws NullPointerException     Thrown if {@code folder} is {@code null}.
	 * @throws IllegalArgumentException Thrown when {@code folder} does not exist
	 *                                  and {@code mkdirs} is {@code false} or,
	 *                                  {@code folder} is not a file directory.
	 */
	public void setPluginDirectory(@Nonnull File folder, boolean mkdirs) {
		// Check if folder is null
		Objects.requireNonNull(folder);

		// Check if file exists
		if (!folder.exists())
			// Create needed directories if selected
			if (mkdirs)
				folder.mkdirs();
			else
				// Folder does not exist
				throw new IllegalArgumentException("File does not exist!");

		// Ensure file is a directory
		if (!folder.isDirectory())
			throw new IllegalArgumentException("File is not a folder!");

		// Set new folder
		this.folder = folder;
	}

	/**
	 * Get the plugin folder currently selected.
	 * 
	 * @return Returns the {@link File} selected as the plugin directory
	 */
	public File getPluginDirectory() {
		return folder;
	}

	/**
	 * Get the class of {@link T}.
	 * 
	 * @return
	 */
	public Class<T> getPluginClass() {
		return pluginClass;
	}

	@Override
	public void close() throws Exception {
		if (loader != null)
			loader.close();
	}

	/**
	 * Get all the jar files in a folder.
	 * 
	 * @param folder - The {@link File} to search in
	 * @return Returns an array of jar {@link File Files}
	 * @throws SecurityException If a security manager exists and its
	 *                           {@link SecurityManager#checkRead(String)} method
	 *                           denies read access to the directory
	 */
	@CheckForNull
	private static File[] getJarsInFolder(@Nonnull File folder) {
		Objects.nonNull(folder);

		return folder.listFiles(filter);
	}

	/**
	 * Map {@link File} array to {@link URL} array.
	 * 
	 * @param files - {@link File} array to map
	 * @return Returns a {@link URL} array mapped from {@code files}.
	 * @throws MalformedURLException If a protocol handler for the URL could not be
	 *                               found,or if some other error occurred while
	 *                               constructing the URL
	 */
	@Nonnull
	private static URL[] filesToURLArray(@Nonnull File[] files) {
		Objects.nonNull(files);

		// URL output list
		URL[] urls = new URL[files.length];

		// Map file array to URL array
		for (int i = 0; i < files.length; i++)
			try {
				urls[i] = files[i].toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		return urls;
	}
}
