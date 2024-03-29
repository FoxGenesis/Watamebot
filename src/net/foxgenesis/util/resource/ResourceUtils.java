package net.foxgenesis.util.resource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
import java.util.function.Function;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.JSONConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.FileBasedBuilderProperties;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class containing methods for handling resources.
 *
 * @author Ashley
 *
 */
public final class ResourceUtils {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

	/**
	 * Read all lines from a resource
	 *
	 * @param path - {@link URL} path to the resource
	 *
	 * @return Returns all lines as a {@link List}
	 *
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
	 * Parse a {@link Properties} file at the specified {@link Path}. If the
	 * specified file was not found, the <i>defaults</i> will be written to its
	 * location.
	 *
	 * @param path     - path to the properties file
	 * @param defaults - (optional) resource containing the default properties file
	 *
	 * @return Returns a {@link Properties} file that was parsed from the specified
	 *         path
	 *
	 * @throws IOException           If an I/O error occurs
	 * @throws FileNotFoundException If the specified file was not found and no
	 *                               defaults were given
	 */
	public static Properties getProperties(Path path, ModuleResource defaults) throws IOException {
		// If file does not exist, create a new one and try to open it again
		if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
			// If defaults are specified, save defaults to the specified path and try again
			if (defaults != null) {
				defaults.writeToFile(path);
				return getProperties(path, defaults);
			}
			// No defaults were specified and the file was not found
			throw new FileNotFoundException("The specified file was not found: " + path);
		}

		// Create and parse the properties file
		Properties properties = new Properties();
		try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
			properties.load(in);
		}
		return properties;
	}

	/**
	 * Load a configuration file of the provided {@code type} from the specified
	 * directory and file. If the file is not found, the provided
	 * {@link ModuleResource} pointing to the configuration defaults will be used
	 * instead.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * Configuration configuration = switch (type) {
	 * 	case PROPERTIES -> loadProperties(defaults, directory, output);
	 * 	case INI -> loadINI(defaults, directory, output);
	 * 	case JSON -> loadJSON(defaults, directory, output);
	 * 	case XML -> loadXML(defaults, directory, output);
	 * 	default -> throw new IllegalArgumentException("Unknown type: " + type);
	 * };
	 * </pre>
	 *
	 * @param type      - configuration type
	 * @param defaults  - resource containing the configuration defaults
	 * @param directory - the directory containing the configuration file
	 * @param output    - the name of the configuration file
	 *
	 * @return Returns the parsed {@link Configuration}
	 *
	 * @throws IOException            If an I/O error occurs
	 * @throws ConfigurationException If an error occurs
	 * @throws SecurityException      Thrown if the specified file is not readable
	 *
	 * @see #loadProperties(ModuleResource, Path, String)
	 * @see #loadINI(ModuleResource, Path, String)
	 * @see #loadJSON(ModuleResource, Path, String)
	 * @see #loadXML(ModuleResource, Path, String)
	 */
	public static Configuration loadConfiguration(ConfigType type, ModuleResource defaults, Path directory,
			String output) throws IOException, ConfigurationException {
		return switch (type) {
			case PROPERTIES -> loadProperties(defaults, directory, output);
			case INI -> loadINI(defaults, directory, output);
			case JSON -> loadJSON(defaults, directory, output);
			case XML -> loadXML(defaults, directory, output);
			default -> throw new IllegalArgumentException("Unknown type: " + type);
		};
	}

	/**
	 * Load a {@code .properties} configuration file from the specified directory
	 * and file. If the file is not found, the provided {@link ModuleResource}
	 * pointing to the configuration defaults will be used instead.
	 *
	 * @param defaults - resource containing the configuration defaults
	 * @param dir      - the directory containing the configuration file
	 * @param output   - the name of the configuration file
	 *
	 * @return Returns the parsed {@link PropertiesConfiguration}
	 *
	 * @throws IOException            If an I/O error occurs
	 * @throws ConfigurationException If an error occurs
	 * @throws SecurityException      Thrown if the specified file is not readable
	 */
	public static PropertiesConfiguration loadProperties(ModuleResource defaults, Path dir, String output)
			throws IOException, ConfigurationException {
		return loadConfig(defaults, dir, output, PropertiesConfiguration.class,
				out -> new Parameters().properties().setBasePath(dir.toString()).setPath(out));
	}

	/**
	 * Load an {@code .ini} configuration file from the specified directory and
	 * file. If the file is not found, the provided {@link ModuleResource} pointing
	 * to the configuration defaults will be used instead.
	 *
	 * @param defaults - resource containing the configuration defaults
	 * @param dir      - the directory containing the configuration file
	 * @param output   - the name of the configuration file
	 *
	 * @return Returns the parsed {@link INIConfiguration}
	 *
	 * @throws IOException            If an I/O error occurs
	 * @throws ConfigurationException If an error occurs
	 * @throws SecurityException      Thrown if the specified file is not readable
	 */
	public static INIConfiguration loadINI(ModuleResource defaults, Path dir, String output)
			throws IOException, ConfigurationException {
		return loadConfig(defaults, dir, output, INIConfiguration.class,
				out -> new Parameters().ini().setBasePath(dir.toString()).setPath(out));
	}

	/**
	 * Load a {@code .json} configuration file from the specified directory and
	 * file. If the file is not found, the provided {@link ModuleResource} pointing
	 * to the configuration defaults will be used instead.
	 *
	 * @param defaults - resource containing the configuration defaults
	 * @param dir      - the directory containing the configuration file
	 * @param output   - the name of the configuration file
	 *
	 * @return Returns the parsed {@link JSONConfiguration}
	 *
	 * @throws IOException            If an I/O error occurs
	 * @throws ConfigurationException If an error occurs
	 * @throws SecurityException      Thrown if the specified file is not readable
	 */
	public static JSONConfiguration loadJSON(ModuleResource defaults, Path dir, String output)
			throws IOException, ConfigurationException {
		return loadConfig(defaults, dir, output, JSONConfiguration.class,
				out -> new Parameters().hierarchical().setBasePath(dir.toString()).setPath(out));
	}

	/**
	 * Load a {@code .xml} configuration file from the specified directory and file.
	 * If the file is not found, the provided {@link ModuleResource} pointing to the
	 * configuration defaults will be used instead.
	 *
	 * @param defaults - resource containing the configuration defaults
	 * @param dir      - the directory containing the configuration file
	 * @param output   - the name of the configuration file
	 *
	 * @return Returns the parsed {@link XMLConfiguration}
	 *
	 * @throws IOException            If an I/O error occurs
	 * @throws ConfigurationException If an error occurs
	 * @throws SecurityException      Thrown if the specified file is not readable
	 */
	public static XMLConfiguration loadXML(ModuleResource defaults, Path dir, String output)
			throws IOException, ConfigurationException {
		return loadConfig(defaults, dir, output, XMLConfiguration.class,
				out -> new Parameters().xml().setBasePath(dir.toString()).setPath(out));
	}

	/**
	 * Load a {@link Configuration} file from the specified directory and file name.
	 * If the file is not found, the specified {@link ModuleResource} pointing to a
	 * default {@link Configuration} will be used instead.
	 *
	 * @param <T>                Configuration type
	 * @param defaults           - configuration defaults
	 * @param dir                - directory containing the file
	 * @param output             - the name of the file
	 * @param configurationClass - the {@link Class} of the configuration
	 * @param paramBuilder       - method that provides the required
	 *                           {@link BuilderParameters} based on the specified
	 *                           output file
	 *
	 * @return Returns the created {@link Configuration} of the specified type
	 *
	 * @throws IOException            If an I/O error occurs
	 * @throws ConfigurationException If an error occurs
	 * @throws SecurityException      Thrown if the specified file is not readable
	 */
	private static <T extends FileBasedConfiguration> T loadConfig(ModuleResource defaults, Path dir, String output,
			Class<T> configurationClass, Function<String, ? extends FileBasedBuilderProperties<?>> paramBuilder)
			throws IOException, ConfigurationException {
		// Save our default configuration file if not exist
		Path outputPath = writeDefaults(defaults, dir, output);
		return new FileBasedConfigurationBuilder<>(configurationClass)
				.configure((BuilderParameters) paramBuilder.apply(outputPath.toAbsolutePath().toString()))
				.getConfiguration();
	}

	/**
	 * Write the default configuration file to the specified directory and file
	 * name.
	 *
	 * @param defaults - {@link ModuleResource} pointing to the default
	 *                 configuration file
	 * @param dir      - output directory
	 * @param output   - output file name
	 *
	 * @return Returns a {@link Path} pointing to the new or existing configuration
	 *         file
	 *
	 * @throws IOException              If an I/O error occurs
	 * @throws IllegalArgumentException Thrown if the specified file is a directory
	 * @throws SecurityException        Thrown if the specified file is not readable
	 */
	private static Path writeDefaults(ModuleResource defaults, Path dir, String output) throws IOException {
		Path outputPath = dir.resolve(output).toAbsolutePath();
		Path finalFolder = outputPath.getParent();

		// Create plugin configuration folder
		if (Files.notExists(finalFolder, LinkOption.NOFOLLOW_LINKS))
			Files.createDirectories(finalFolder);

		// Create configuration file
		if (Files.notExists(outputPath, LinkOption.NOFOLLOW_LINKS))
			defaults.writeToFile(outputPath);
		else if (Files.isDirectory(outputPath, LinkOption.NOFOLLOW_LINKS))
			throw new IllegalArgumentException(outputPath.toString() + " is not a regular file!");
		else if (!Files.isReadable(outputPath))
			throw new SecurityException("Unable to read " + outputPath.toString() + ". Missing permissions!");

		return outputPath;
	}

	/**
	 * Read all data from the specified {@link InputStream} and parse it as a
	 * string.
	 * <p>
	 * The input stream will be closed after completion
	 * </p>
	 *
	 * @param input - the input stream to read
	 *
	 * @return Returns a string containing the data from the specified input stream
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public static String toString(@NotNull InputStream input) throws IOException {
		try (input) {
			return new String(input.readAllBytes());
		}
	}

	/**
	 * Read all data from the specified {@link URL} and parse it as a string.
	 * <p>
	 * The input stream will be closed after completion
	 * </p>
	 *
	 * @param url - the {@link URL} to read from
	 *
	 * @return Returns a string containing the data from the specified URL
	 *
	 * @throws IOException If an I/O error occurs
	 */
	@SuppressWarnings("resource")
	public static String toString(@NotNull URL url) throws IOException {
		return toString(url.openStream());
	}

	/**
	 * Read all data from the specified {@link InputStream} and parse all lines as a
	 * string array.
	 * <p>
	 * The input stream will be closed after completion
	 * </p>
	 *
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * toString(input).split("(\\r\\n|\\r|\\n)")
	 * </pre>
	 *
	 * @param input - the input stream to read
	 *
	 * @return Returns a string array containing the data from the specified input
	 *         stream
	 *
	 * @throws IOException If an I/O error occurs
	 */
	public static String[] toSplitString(@NotNull InputStream input) throws IOException {
		return toString(input).split("(\\r\\n|\\r|\\n)");
	}
}
