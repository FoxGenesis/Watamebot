package net.foxgenesis.property;

import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface defining methods for retrieving properties from a configuration.
 *
 * @param <L> Property lookup type
 * @param <C> Property category type
 * @param <K> Property key type
 *
 * @author Ashley
 */
public interface PropertyResolver<L, C, K> {

	/**
	 * Max length for category values
	 */
	int MAX_CATEGORY_LENGTH = 50;

	/**
	 * Max length for key values
	 */
	int MAX_KEY_LENGTH = 500;

	/**
	 * Create a new property inside the configuration.
	 *
	 * @param category   - category of the property
	 * @param key        - property key
	 * @param modifiable - is the property user modifiable
	 * @param type       - blob object type
	 *
	 * @return Returns the created {@link PropertyInfo}
	 *
	 * @throws PropertyException        Thrown if the property already exists or
	 *                                  there was an internal error
	 * @throws IllegalArgumentException Thrown if the property already is registered
	 */
	PropertyInfo createPropertyInfo(@NotNull C category, @NotNull K key, boolean modifiable, @NotNull PropertyType type)
			throws PropertyException, IllegalArgumentException;

	/**
	 * Remove a property inside the configuration.
	 * 
	 * @param category - category of the property
	 * @param key      - property key
	 * 
	 * @return Returns {@code true} if the property was deleted. {@code false}
	 *         otherwise
	 */
	boolean removePropertyInfo(@NotNull C category, @NotNull K key);

	/**
	 * Remove a property inside the configuration.
	 * 
	 * @param info - property information
	 * 
	 * @return Returns {@code true} if the property was deleted. {@code false}
	 *         otherwise
	 */
	boolean removePropertyInfo(@NotNull PropertyInfo info);

	/**
	 * Check if the specified property exists inside the configuration.
	 *
	 * @param category - category of the property
	 * @param key      - property key
	 *
	 * @return Returns {@code true} if the property already exists inside the
	 *         configuration
	 *
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	boolean isRegistered(@NotNull C category, @NotNull K key) throws PropertyException;

	/**
	 * Get the property linked to the specified {@code id}.
	 *
	 * @param id - property id
	 *
	 * @return Returns the {@link PropertyInfo} bound to the id
	 *
	 * @throws PropertyException      Thrown if an internal error occurs
	 * @throws NoSuchElementException Thrown if the specified property does not
	 *                                exist
	 */
	PropertyInfo getPropertyByID(int id) throws PropertyException, NoSuchElementException;

	/**
	 * Get the property information for the specified property.
	 *
	 * @param category - category of the property
	 * @param key      - property key
	 *
	 * @return Returns the {@link PropertyInfo} contained in the configuration if
	 *         present
	 *
	 * @throws PropertyException      Thrown if an internal error occurs
	 * @throws NoSuchElementException Thrown if the specified property does not
	 *                                exist
	 */
	PropertyInfo getPropertyInfo(@NotNull C category, @NotNull K key) throws PropertyException, NoSuchElementException;

	/**
	 * Get a {@link List} of all registered properties.
	 *
	 * @return Returns a {@link List} containing the {@link PropertyInfo} of all
	 *         registered properties
	 *
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	@NotNull
	List<PropertyInfo> getPropertyList() throws PropertyException;

	/**
	 * Remove an internal value from the configuration.
	 *
	 * @param lookup - property lookup
	 * @param info   - property information
	 *
	 * @return Returns {@code true} if the property was successfully removed from
	 *         the configuration
	 *
	 * @see #putInternal(Object, PropertyInfo, InputStream)
	 * @see #getInternal(Object, PropertyInfo)
	 *
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	boolean removeInternal(@NotNull L lookup, @NotNull PropertyInfo info) throws PropertyException;

	/**
	 * Put/Update an internal property inside the configuration.
	 *
	 * @param lookup - property lookup
	 * @param info   - property information
	 * @param in     - data stream to write
	 *
	 * @return Returns {@code true} if the property was successfully added/updated
	 *         in the configuration
	 *
	 * @see #removeInternal(Object, PropertyInfo)
	 * @see #getInternal(Object, PropertyInfo)
	 *
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	boolean putInternal(@NotNull L lookup, @NotNull PropertyInfo info, @Nullable InputStream in)
			throws PropertyException;

	/**
	 * Get an internal property inside the configuration.
	 *
	 * @param lookup - property lookup
	 * @param info   - property information
	 *
	 * @return Returns an {@link Optional} containing raw value data
	 *
	 * @see #removeInternal(Object, PropertyInfo)
	 * @see #putInternal(Object, PropertyInfo, InputStream)
	 *
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	@NotNull
	Optional<?> getInternal(@NotNull L lookup, @NotNull PropertyInfo info) throws PropertyException;

	/**
	 * Check if the specified property is present in the configuration.
	 * <p>
	 * <b>Note:</b> This method only checks if the property key was found. The
	 * property may still a {@code null} value
	 * </p>
	 *
	 * @param lookup - property lookup
	 * @param info   - property information
	 *
	 * @return Returns {@code true} if this property was found inside the
	 *         configuration
	 *
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	boolean isPresent(@NotNull L lookup, @NotNull PropertyInfo info) throws PropertyException;
}
