package net.foxgenesis.watame.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to load a multiple plugin classes. Classes are loaded from jar files in
 * a selected directory. Target classes are selected via a properties file.
 * 
 * @author Ashley
 *
 */
public class PluginLoader {

	private static Logger logger = LoggerFactory.getLogger(PluginLoader.class);

	/**
	 * Plugin folder
	 */
	private File folder;

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
	public PluginLoader(@Nonnull File folder) {
		this(folder, false);
	}

	/**
	 * Create a new {@link PluginLoader} instance and set the directory to use.
	 * 
	 * @param folder - The {@link File} to search for plugins in.
	 * @param mkdirs - If {@code folder} does not exist, should a new one be made
	 */
	public PluginLoader(@Nonnull File folder, boolean mkdirs) {
		setPluginDirectory(folder, mkdirs);
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
	 * Get the plugin folder currently selected
	 * 
	 * @return Returns the {@link File} selected as the plugin directory
	 */
	public File getPluginDirectory() {
		return folder;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public <T> Map<Class<? extends T>, Properties> getPluginClasses(@Nonnull Class<? extends T> pluginClass) {
		Objects.nonNull(pluginClass);

		// Get files and map to URLs
		URL[] pluginURLs = filesToURLArray(getJarsInFolder(folder));

		// Create output list
		Map<Class<? extends T>, Properties> classes = new HashMap<>(pluginURLs.length);

		// Create new class loader with plugin URL array
		try (URLClassLoader loader = new URLClassLoader(pluginURLs)) {

			// Get all plugin properties files
			Enumeration<URL> resources = loader.findResources("/plugin.properties");

			// Iterate over properties files
			while (resources.hasMoreElements()) {
				// Get current property file
				URL resource = resources.nextElement();

				// Open and populate property file
				Properties properties = new Properties();
				try (InputStream is = resource.openStream()) {
					properties.load(is);
				} catch (IOException e1) {
					logger.error("Unable to load property file from " + resource, e1);
					continue;
				}

				// Get plugin class from properties file
				String className = properties.getProperty("plugin-class");

				// Check if plugin class property is set
				if (className == null) {
					logger.error("plugin-class missing from plugin.properties in " + resource,
							new InvalidPluginPropertiesException());
					continue;
				}

				try {
					// Attempt to load plugin class
					Class<?> pluginMain = loader.loadClass(className);

					// Check whether the plugin class extends APlugin
					if (pluginClass.isAssignableFrom(pluginMain)) {
						// Cast and add class to our plugin list
						Class<? extends T> b = pluginMain.asSubclass(pluginClass);
						classes.put(b, properties);
					} else {
						// Class specified in properties doesn't extend APlugin
						logger.error("Plugin class \"" + className + "\" does not extend APlugin!",
								new InvalidPluginPropertiesException());
						continue;
					}
				} catch (ClassNotFoundException e) {
					// Plugin class was not found
					logger.error("Plugin class \"" + className + "\" does not exist!", e);
				}

			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		return classes;
	}

	/**
	 * Get all the jar files in a folder.
	 * 
	 * @param folder - The {@link File} to search in
	 * @return Returns an array of jar {@link File Files}
	 */
	private File[] getJarsInFolder(@Nonnull File folder) {
		return folder.listFiles(file -> file.getName().endsWith(".jar"));
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
	private URL[] filesToURLArray(@Nonnull File[] files) {
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
