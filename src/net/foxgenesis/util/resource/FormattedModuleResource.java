package net.foxgenesis.util.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * A class that points to a resource inside a module and implements methods to
 * read from it after formatting its data.
 * <p>
 * Properties to format are specified by using a regular expression:
 * </p>
 * <blockquote>
 * 
 * <pre>
 * \{\{(.*?)}}
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Ashley
 *
 */
public class FormattedModuleResource extends ModuleResource {
	/**
	 * Property regular expression
	 */
	private static final Pattern pattern = Pattern.compile("\\{\\{(.*?)}}");

	/**
	 * Property mappings
	 */
	private final Map<String, String> mappings;

	/**
	 * Create a new {@link ModuleResource} that points to a specified
	 * {@code resource} inside a {@code module}.
	 * <p>
	 * The specified {@link Map} will be used to format all data in this resource.
	 * </p>
	 * 
	 * @param module   - name of the {@link Module} containing the resource
	 * @param resource - absolute path to the resource
	 * @param mappings - a {@link Map} containing all the properties to format
	 * 
	 * @throws NullPointerException If the module name, resource name or mappings
	 *                              are null
	 */
	public FormattedModuleResource(@Nonnull String module, @Nonnull String resource,
			@Nonnull Map<String, String> mappings) {
		super(module, resource);
		this.mappings = Objects.requireNonNull(mappings);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the specified mappings is empty, this will operate the same as
	 * {@link ModuleResource#openStream()}. Otherwise, all data in the stream will
	 * be read into a string and formatted with the specified mappings.
	 * </p>
	 */
	@Override
	public InputStream openStream() throws IOException {
		if (mappings.isEmpty())
			return super.openStream();
		try (InputStream in = super.openStream()) {
			return new ByteArrayInputStream(format(new String(in.readAllBytes())).getBytes());
		}

	}

	/**
	 * Format a string will all the provided mappings.
	 * 
	 * @param str - the string to format
	 * 
	 * @return The formatted string
	 */
	private String format(String str) {
		return pattern.matcher(str).replaceAll(result -> mappings.getOrDefault(result.group(1), "null"));
	}
}
