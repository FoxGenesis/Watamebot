package net.foxgenesis.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.Nonnull;

import net.foxgenesis.util.ResourceUtils;

/**
 * A class that points to a resource inside a module and implements methods to
 * read from it.
 * 
 * @author Ashley
 *
 */
public class ModuleResource {
	/**
	 * Module this resource is located in
	 */
	private final Module module;

	/**
	 * Path to resource inside module
	 */
	private final String path;

	/**
	 * Create a new {@link ModuleResource} that points to a specified
	 * {@code resource} inside a {@code module}.
	 * 
	 * @param module   - {@link Module} containing the resource
	 * @param resource - absolute path to the resource
	 */
	public ModuleResource(@Nonnull Module module, @Nonnull String resource) {
		this.module = Objects.requireNonNull(module);
		this.path = Objects.requireNonNull(resource);
	}

	/**
	 * Create a new {@link ModuleResource} that points to a specified
	 * {@code resource} inside a {@code module}.
	 * 
	 * @param module   - name of the {@link Module} containing the resource
	 * @param resource - absolute path to the resource
	 */
	public ModuleResource(@Nonnull String moduleName, @Nonnull String resourcePath) throws NoSuchElementException {
		this(ModuleLayer.boot().findModule(moduleName)
				.orElseThrow(() -> new NoSuchElementException("No module found '" + moduleName + "'")), resourcePath);
	}

	/**
	 * Open an {@link InputStream} to this resource
	 * 
	 * @return A
	 * @throws IOException
	 */
	public InputStream openStream() throws IOException { return module.getResourceAsStream(path); }

	public void writeToFile(@Nonnull Path path, CopyOption... options) throws IOException {
		try (InputStream in = openStream()) {
			Files.copy(in, path, options);
		}
	}

	@SuppressWarnings("resource")
	public String readToString() throws IOException { return ResourceUtils.toString(openStream()); }

	@SuppressWarnings("resource")
	public String[] readAllLines() throws IOException { return ResourceUtils.toSplitString(openStream()); }

	public Properties asProperties() throws IOException {
		Properties properties = new Properties();
		try (InputStream in = openStream()) {
			properties.load(in);
		}
		return properties;
	}

	@Override
	@Nonnull
	public String toString() { return module.getName() + ":" + path; }
}