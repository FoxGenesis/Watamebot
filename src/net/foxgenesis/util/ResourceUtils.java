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
import java.util.Properties;

import javax.annotation.Nonnull;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.util.resource.ModuleResource;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
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

	/**
	 * NEED_JAVADOC
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Properties getProperties(URL path) throws IOException {
		logger.trace("Attempting to read resource: " + path);

		Properties properties = new Properties();
		try (InputStream in = path.openStream()) {
			properties.load(in);
		}

		return properties;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param path
	 * @param defaults
	 * @return
	 * @throws IOException
	 */
	public static Properties getProperties(Path path, ModuleResource defaults) throws IOException {
		// If file does not exist, create a new one and try to open it again
		if (defaults != null && Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
			defaults.writeToFile(path);
			return getProperties(path, defaults);
		}

		Properties properties = new Properties();
		try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
			properties.load(in);
		}
		return properties;
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param defaults
	 * @param dir
	 * @param output
	 * @return
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static PropertiesConfiguration loadConfig(ModuleResource defaults, Path dir, String output)
			throws IOException, ConfigurationException {
		// Create plugin configuration folder
		if (Files.notExists(dir, LinkOption.NOFOLLOW_LINKS))
			Files.createDirectories(dir);

		Path outputPath = dir.resolve(output);
		// Create configuration file
		if (Files.notExists(outputPath, LinkOption.NOFOLLOW_LINKS))
			defaults.writeToFile(outputPath);
		else if (Files.isDirectory(outputPath, LinkOption.NOFOLLOW_LINKS))
			throw new IllegalArgumentException(outputPath.toString() + " is not a regular file!");
		else if (!Files.isReadable(outputPath))
			throw new SecurityException("Unable to read " + outputPath.toString() + ". Missing permissions!");

		// Load configuration file
		PropertiesBuilderParameters propParams = new Parameters().properties();
		propParams.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		propParams.setBasePath(dir.toString());
		propParams.setPath(outputPath.toString());

		FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
				PropertiesConfiguration.class);

		builder.configure(propParams);

		return builder.getConfiguration();
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static String toString(@Nonnull InputStream input) throws IOException {
		try (input) {
			return new String(input.readAllBytes());
		}
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static String[] toSplitString(@Nonnull InputStream input) throws IOException {
		return toString(input).split("(\\r\\n|\\r|\\n)");
	}
}
