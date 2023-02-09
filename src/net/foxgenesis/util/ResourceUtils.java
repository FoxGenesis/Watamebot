package net.foxgenesis.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.Nonnull;

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
	
	public static Properties getProperties(Path path, ModuleResource defaults) throws IOException {
		// If file does not exist, create a new one and try to open it again
		if(Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
			defaults.writeToFile(path);
			return getProperties(path, defaults);
		}
		
		Properties properties = new Properties();
		properties.load(Files.newInputStream(path, StandardOpenOption.READ));
		return properties;
	}

	public static String toString(@Nonnull InputStream input) throws IOException {
		Objects.requireNonNull(input, "InputStream must not be null!");
		return new String(input.readAllBytes());
	}

	public static String[] toSplitString(@Nonnull InputStream input) throws IOException {
		return toString(input).split("(\\r\\n|\\r|\\n)");
	}

	public record ModuleResource(Module module, String resourcePath) {
		public ModuleResource(String moduleName, String resourcePath) {
			this(ModuleLayer.boot().findModule(moduleName).orElseThrow(
					() -> new NoSuchElementException("No module found '" + moduleName + "'")), resourcePath);
		}

		public InputStream openStream() throws IOException {
			logger.trace("Attempting to read resource: " + resourcePath);
			return module.getResourceAsStream(resourcePath);
		}
		
		public void writeToFile(Path path) throws IOException {
			Files.copy(openStream(), path);
		}

		public String readToString() throws IOException { return ResourceUtils.toString(openStream()); }

		public String[] readAllLines() throws IOException { return ResourceUtils.toSplitString(openStream()); }

		public Properties asProperties() throws IOException {
			Properties properties = new Properties();
			properties.load(openStream());

			return properties;
		}
	}
}
