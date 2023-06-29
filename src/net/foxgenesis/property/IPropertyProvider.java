package net.foxgenesis.property;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * An interface containing methods used for constructing/providing
 * {@link ImmutableProperty IPropertyFields}.
 * 
 * @author Ashley
 *
 * @param <K> Property key type
 * @param <L> Property look up data type
 * @param <M> {@link IPropertyMapping} data type
 * @see ImmutableProperty
 * @see IPropertyMapping
 */
public interface IPropertyProvider<K, L, M extends IPropertyMapping> {

	/**
	 * Get a property field associated with a specified key.
	 * 
	 * @param key - the property's key
	 * @return Returns an {@link ImmutableProperty} pointing to the specified key
	 */
	@NotNull
	IProperty<K, L, M> getProperty(@NotNull K key);

	/**
	 * Check if the property associated with the specified key has already been
	 * constructed.
	 * 
	 * @param key - the property's key
	 * @return Returns {@code true} if the property's key has already been
	 *         registered/constructed.
	 */
	boolean isPropertyPresent(@NotNull K key);

	/**
	 * Get a set of all registered keys this provider has constructed.
	 * 
	 * @return Returns a {@link Set} of {@link K}
	 */
	@NotNull
	Set<K> keySet();
}
