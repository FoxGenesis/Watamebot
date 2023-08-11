package net.foxgenesis.property;

import java.util.List;

import net.foxgenesis.property.lck.LCKProperty;

import org.jetbrains.annotations.NotNull;

/**
 * Base interface defining methods to provide and register {@link LCKProperty}
 * and {@link PropertyInfo}.
 * 
 * @param <C> property category type
 * @param <K> property key type
 * 
 * @author Ashley
 */
public interface PropertyProvider<C, K, G, M extends PropertyMapping> {
	/**
	 * Register a new {@link PropertyInfo} inside the configuration.
	 * 
	 * @param category   - property owner
	 * @param key        - property key
	 * @param modifiable - if can be modified by the user
	 * @param type       - property storage type
	 * 
	 * @return Returns the created {@link PropertyInfo}
	 */
	PropertyInfo registerProperty(@NotNull C category, @NotNull K key, boolean modifiable, @NotNull PropertyType type);

	/**
	 * Register a {@link PropertyInfo} if it does not exist inside the configuration
	 * and return a new {@link LCKProperty} created with the property information.
	 * 
	 * @param category   - property owner
	 * @param key        - property key
	 * @param modifiable - if can be modified by the end user
	 * @param type       - property storage type
	 * 
	 * @return Returns a {@link LCKProperty} with the specified {@link PropertyInfo}
	 */
	default Property<G, M> upsertProperty(@NotNull C category, @NotNull K key, boolean modifiable,
			@NotNull PropertyType type) {
		if (!propertyExists(category, key))
			registerProperty(category, key, modifiable, type);
		return getProperty(category, key);
	}

	/**
	 * Get a {@link LCKProperty} based on the specified property information
	 * {@code id}.
	 * 
	 * @param id - {@link PropertyInfo} id
	 * 
	 * @return Returns a {@link LCKProperty} using the specified
	 *         {@link PropertyInfo} {@code id}
	 */
	Property<G, M> getPropertyByID(int id);

	/**
	 * Get a {@link LCKProperty} based on the specified property {@code category}
	 * and {@code key}.
	 * 
	 * @param category - property owner
	 * @param key      - property key
	 * 
	 * @return Returns a {@link LCKProperty} linked to the {@link PropertyInfo}
	 *         {@code category} and {@code name}
	 */
	Property<G, M> getProperty(@NotNull C category, @NotNull K key);

	/**
	 * Get a {@link LCKProperty} linked to the specified {@link PropertyInfo}.
	 * 
	 * @param info - property information
	 * 
	 * @return Returns a {@link LCKProperty} with the specified {@code info}
	 */
	Property<G, M> getProperty(@NotNull PropertyInfo info);

	/**
	 * Check if a {@link PropertyInfo} is registered inside the configuration
	 * 
	 * @param category - property owner
	 * @param key      - property key
	 * 
	 * @return Returns {@code true} if the property already exists inside the
	 *         configuration, otherwise {@code false}
	 */
	boolean propertyExists(@NotNull C category, @NotNull K key);

	/**
	 * Get a list of all registered {@link PropertyInfo}.}
	 * 
	 * @return Returns a {@link List} of all {@link PropertyInfo} registered inside
	 *         the configuration
	 */
	List<PropertyInfo> getPropertyList();
}
