package net.foxgenesis.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResourceUtils {

	private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

	/**
	 * Read all lines from a resource
	 *
	 * @param path - {@link URL} path to the resource
	 * @return Returns all lines as a {@link List<String>}
	 * @throws IOException Thrown if an error occurs while reading the
	 *                     {@link InputStream} of the resource
	 */
	public static List<String> linesFromResource(URL path) throws IOException {
		logger.trace("Attempting to read resource: " + path);

		// New list to hold lines
		ArrayList<String> list = new ArrayList<>();

		// Open bufferedReader from resource input stream
		try (InputStreamReader isr = new InputStreamReader(path.openStream());
				BufferedReader reader = new BufferedReader(isr)) {

			// Temp line
			String line = null;

			// Read line until EOF
			while ((line = reader.readLine()) != null)
				list.add(line);

			// Return list
			return list;
		}
	}

	public static Properties getPropertiesResource(URL path) throws IOException {
		logger.trace("Attempting to read resource: " + path);

		Properties properties = new Properties();
		properties.load(path.openStream());

		return properties;
	}

	public record ModuleResource(Module module, String resourcePath) {
		public ModuleResource(String moduleName, String resourcePath) {
			this(ModuleLayer.boot().findModule(moduleName).orElseThrow(), resourcePath);
		}
		
		public InputStream openStream() throws IOException {
			return module.getResourceAsStream(resourcePath);
		}
	}
}
