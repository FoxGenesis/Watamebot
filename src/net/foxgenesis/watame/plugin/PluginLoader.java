package net.foxgenesis.watame.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public PluginLoader(File folder) {
		this(folder, false);
	}

	/**
	 * Create a new {@link PluginLoader} instance and set the directory to use.
	 * 
	 * @param folder - The {@link File} to search for plugins in.
	 * @param mkdirs - If {@code folder} does not exist, should a new one be made
	 */
	public PluginLoader(File folder, boolean mkdirs) {
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
	public void setPluginDirectory(File folder, boolean mkdirs) {
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
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public List<Class<? extends APlugin>> getPluginClasses() throws MalformedURLException, IOException {
		// Get files and map to URLs
		URL[] pluginURLs = filesToURLArray(getJarsInFolder(folder));

		// Create output list
		List<Class<? extends APlugin>> classes = new ArrayList<>(pluginURLs.length);

		// Create new class loader with plugin URL array
		try (URLClassLoader loader = new URLClassLoader(pluginURLs)) {

			// Get all plugin properties files
			Enumeration<URL> resources = loader.findResources("/META-INFO/plugin.properties");

			// Iterate over properties files
			while (resources.hasMoreElements()) {
				// Get current property file
				URL resource = resources.nextElement();

				// Open and populate property file
				Properties properties = new Properties();
				try (InputStream is = resource.openStream()) {
					properties.load(is);
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
					Class<?> pluginClass = loader.loadClass(className);

					// Check whether the plugin class extends APlugin
					if (APlugin.class.isAssignableFrom(pluginClass)) {
						// Cast and add class to our plugin list
						Class<? extends APlugin> b = pluginClass.asSubclass(APlugin.class);
						classes.add(b);
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
		}

		return classes;
	}

	/**
	 * Get all the jar files in a folder.
	 * 
	 * @param folder - The {@link File} to search in
	 * @return Returns an array of jar {@link File Files}
	 */
	private File[] getJarsInFolder(File folder) {
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
	private URL[] filesToURLArray(File[] files) throws MalformedURLException {
		// URL output list
		URL[] urls = new URL[files.length];

		// Map file array to URL array
		for (int i = 0; i < files.length; i++)
			urls[i] = files[i].toURI().toURL();

		return urls;
	}
}
