package net.foxgenesis.property3.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import net.foxgenesis.property3.PropertyException;
import net.foxgenesis.property3.PropertyInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LCKPropertyResolver {

	/**
	 * Remove an internal value from the database.
	 * 
	 * @param lookup   - property lookup
	 * @param property - property to remove
	 * 
	 * @return Returns {@code true} if the property was successfully removed from
	 *         the database
	 * 
	 * @see #putInternal(long, PropertyInfo, Serializable)
	 * @see #putInternal(long, PropertyInfo, InputStream)
	 * @see #getInternal(long, PropertyInfo)
	 * 
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	boolean removeInternal(long lookup, @NotNull PropertyInfo property) throws PropertyException;

	/**
	 * Put/Update an internal property inside the database.
	 * 
	 * @param <U>      object implementing {@link Serializable}
	 * @param lookup   - property lookup
	 * @param property - property to remove
	 * @param value    - value to insert
	 * 
	 * @return Returns {@code true} if the property was successfully added/updated
	 *         in the database
	 * 
	 * @see #removeInternal(long, PropertyInfo)
	 * @see #putInternal(long, PropertyInfo, InputStream)
	 * @see #getInternal(long, PropertyInfo)
	 * 
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	<U extends Serializable> boolean putInternal(long lookup, @NotNull PropertyInfo property, @Nullable U value)
			throws PropertyException;

	/**
	 * Put/Update an internal property inside the database.
	 * 
	 * @param lookup   - property lookup
	 * @param property - property to remove
	 * @param in       - data stream to write
	 * 
	 * @return Returns {@code true} if the property was successfully added/updated
	 *         in the database
	 * 
	 * @see #removeInternal(long, PropertyInfo)
	 * @see #putInternal(long, PropertyInfo, Serializable)
	 * @see #getInternal(long, PropertyInfo)
	 * 
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	boolean putInternal(long lookup, @NotNull PropertyInfo property, @Nullable InputStream in) throws PropertyException;

	/**
	 * Get an internal property inside the database.
	 * 
	 * @param lookup   - property lookup
	 * @param property - property to remove
	 * 
	 * @return Returns an {@link Optional} containing raw value data
	 * 
	 * @see #removeInternal(long, PropertyInfo)
	 * @see #putInternal(long, PropertyInfo, Serializable)
	 * @see #putInternal(long, PropertyInfo, InputStream)
	 * 
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	Optional<Blob> getInternal(long lookup, @NotNull PropertyInfo property) throws PropertyException;

	/**
	 * Check if the specified property is present in the configuration.
	 * <p>
	 * <b>Note:</b> This method only checks if the property key was found. The
	 * property may still a {@code null} value
	 * </p>
	 * 
	 * @param lookup   - property lookup
	 * @param property - property to remove
	 * 
	 * @return Returns {@code true} if this property was found inside the
	 *         configuration
	 * 
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	boolean isPresent(long lookup, @NotNull PropertyInfo property) throws PropertyException;

	/**
	 * Create a new property inside the database.
	 * 
	 * @param category   - category of the property
	 * @param key        - property key
	 * @param modifiable - is the property user modifiable
	 * 
	 * @return Returns the created {@link PropertyInfo}
	 * 
	 * @throws PropertyException Thrown if the property already exists or there was
	 *                           an internal error
	 */
	PropertyInfo createPropertyInfo(@NotNull String category, @NotNull String key, boolean modifiable)
			throws PropertyException;

	/**
	 * Check if the specified property exists inside the database.
	 * 
	 * @param category - category of the property
	 * @param key      - property key
	 * 
	 * @return Returns {@code true} if the property already exists inside the
	 *         database
	 * 
	 * @throws PropertyException Thrown if an internal error occurs
	 */
	boolean propertyInfoExists(@NotNull String category, @NotNull String key) throws PropertyException;

	/**
	 * Get the property information for the specified property.
	 * 
	 * @param category - category of the property
	 * @param key      - property key
	 * 
	 * @return Returns the {@link PropertyInfo} contained in the database if present
	 * 
	 * @throws PropertyException      Thrown if an internal error occurs
	 * @throws NoSuchElementException Thrown if the specified property does not
	 *                                exist
	 */
	@NotNull
	PropertyInfo getPropertyInfo(@NotNull String category, @NotNull String key)
			throws PropertyException, NoSuchElementException;

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
}
