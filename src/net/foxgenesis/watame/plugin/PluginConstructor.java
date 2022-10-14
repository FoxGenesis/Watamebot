package net.foxgenesis.watame.plugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

public class PluginConstructor<T> {

	private static final Logger logger = LoggerFactory.getLogger(PluginConstructor.class);

	private final PluginLoader loader;

	private final Class<T> typeParamaterClass;

	public PluginConstructor(Class<T> typeParamaterClass) {
		loader = new PluginLoader(Constants.pluginFolder, true);

		this.typeParamaterClass = typeParamaterClass;
	}

	public Collection<T> loadPlugins(WatameBot watame) throws MalformedURLException, IOException {
		Map<Class<? extends T>, Properties> classes;
		Set<T> plugins = new HashSet<>();

		logger.trace("Loading plugin classes");
		classes = loader.getPluginClasses(typeParamaterClass);
		logger.debug("Found {} plugin classes", classes.size());

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
				Object obj = pluginConstructor.newInstance();

				T plugin = typeParamaterClass.cast(obj);

				// Set variables
				// plugin.setProperties(new PluginProperties(value));

				try {
					// Try to set the properties field
					Field field = plugin.getClass().getField("properties");

					if (field.getType().equals(PluginProperties.class))
						field.set(plugin, new PluginProperties(value));
					else
						logger.warn("Properties field {} of class {} is not the type of {}! Not setting properties!", field, key, PluginProperties.class);

				} catch (NoSuchFieldException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
