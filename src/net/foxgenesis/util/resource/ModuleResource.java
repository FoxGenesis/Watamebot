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
	 * 
	 * @throws NullPointerException If the module name or resource name are null
	 * 
	 * @see #ModuleResource(String, String)
	 */
	public ModuleResource(@Nonnull Module module, @Nonnull String resource) {
		this.module = Objects.requireNonNull(module);
		this.path = Objects.requireNonNull(resource);
	}

	/**
	 * Create a new {@link ModuleResource} that points to a specified
	 * {@code resource} inside a {@code module}.
	 * 
	 * @param moduleName   - name of the {@link Module} containing the resource
	 * @param resourcePath - absolute path to the resource
	 * 
	 * @throws NullPointerException If the module name or resource name are null
	 * @throws NoSuchElementException If the module was not found
	 * 
	 * @see #ModuleResource(Module, String)
	 */
	public ModuleResource(@Nonnull String moduleName, @Nonnull String resourcePath) throws NoSuchElementException {
		this(ModuleLayer.boot().findModule(moduleName)
				.orElseThrow(() -> new NoSuchElementException("No module found '" + moduleName + "'")), resourcePath);
	}

	/**
	 * Open an {@link InputStream} to this resource.
	 * 
	 * @return Returns an {@link InputStream} of this resource.
	 * 
	 * @throws IOException If an I/O error occurs
	 */
	public InputStream openStream() throws IOException {
		return module.getResourceAsStream(path);
	}

	/**
	 * Copy this resource to the specified path.
	 * 
	 * @param path    - {@link Path} tp copy to
	 * @param options - options specifying how the copy should be done
	 * 
	 * @throws IOException If an I/O error occurs when reading or writing
	 */
	public void writeToFile(@Nonnull Path path, CopyOption... options) throws IOException {
		try (InputStream in = openStream()) {
			Files.copy(in, path, options);
		}
	}

	/**
	 * Read all data from this resource into a string.
	 * 
	 * @return All data inside the resource as a string
	 * 
	 * @throws IOException If an I/O error occurs
	 */
	public String readToString() throws IOException {
		try (InputStream in = openStream()) {
			return new String(in.readAllBytes());
		}
	}

	/**
	 * Read all lines from this resource.
	 * 
	 * @return All data inside the resource as an array of strings
	 * 
	 * @throws IOException If an I/O error occurs
	 */
	public String[] readAllLines() throws IOException {
		return readToString().split("(\\r\\n|\\r|\\n)");
	}

	/**
	 * Read this resource as a {@link Properties} file.
	 * 
	 * @return Returns the parsed {@link Properties}
	 * 
	 * @throws IOException If an I/O error occurs
	 */
	public Properties asProperties() throws IOException {
		Properties properties = new Properties();
		try (InputStream in = openStream()) {
			properties.load(in);
		}
		return properties;
	}

	/**
	 * Get the {@link Module} containing this resource.
	 * 
	 * @return The containing {@link Module}
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * Get the path to this resource.
	 * 
	 * @return Returns the absolute path pointing to this resource
	 */
	public String getResourcePath() {
		return path;
	}

	@Override
	public String toString() {
		return module.getName() + ":" + path;
	}
}