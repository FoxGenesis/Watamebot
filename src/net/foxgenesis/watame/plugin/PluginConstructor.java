package net.foxgenesis.watame.plugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.watame.Constants;
import net.foxgenesis.watame.WatameBot;

public class PluginConstructor {

	private static final Logger logger = LoggerFactory.getLogger(PluginConstructor.class);

	private final PluginLoader loader;

	public PluginConstructor() {
		loader = new PluginLoader(Constants.pluginFolder, true);
	}

	public Collection<AWatamePlugin> loadPlugins(WatameBot watame) throws MalformedURLException, IOException {
		Set<AWatamePlugin> plugins = new HashSet<>();

		logger.debug("Loading plugin classes");
		Map<Class<? extends AWatamePlugin>, Properties> classes = loader.getPluginClasses(AWatamePlugin.class);

		// Construct each class
		classes.forEach((key, value) -> {
			logger.debug("Constructing {}", key);

			// Get constructor
			Constructor<?> pluginConstructor = getDefaultConstructor(key);
			if (pluginConstructor == null) {
				logger.error("Failed to find constructor for " + key.getName(), new NoSuchMethodException());
				return;
			}

			// Create new instance
			try {
				AWatamePlugin plugin = (AWatamePlugin) pluginConstructor.newInstance();
				
				// Set variables
				plugin.setProperties(new PluginProperties(value));
				plugin.setWatame(watame);

				plugins.add(plugin);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		return plugins;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param _class
	 * @return
	 */
	private static Constructor<?> getDefaultConstructor(Class<?> _class) {
		Constructor<?> pluginConstructor = null;

		// Find constructor with zero parameters
		for (Constructor<?> declaredConstructor : _class.getConstructors()) {
			if (declaredConstructor.getParameterCount() == 0) {
				pluginConstructor = declaredConstructor;
				break;
			}
		}

		return pluginConstructor;
	}
}
