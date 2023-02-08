package net.foxgenesis.property;

import javax.annotation.Nonnull;

/**
 * An interface containing methods used for constructing/providing
 * {@link IPropertyField}s.
 * 
 * @author Ashley
 *
 * @param <K> - Property key type
 * @param <F> - Property look up data type
 * @param <M> - {@link IPropertyMapping} data type
 * @see IPropertyField
 * @see IPropertyMapping
 */
public interface IPropertyProvider<K, F, M extends IPropertyMapping> {

	/**
	 * Get a property field associated with a specified key.
	 * 
	 * @param key - the property's key
	 * @return Returns an {@link IPropertyField} pointing to the specified key
	 */
	IPropertyField<K, F, M> getProperty(@Nonnull K key);

	/**
	 * Check if the property associated with the specified key has already been
	 * constructed.
	 * 
	 * @param key - the property's key
	 * @return Returns {@code true} if the property's key has already been
	 *         registered/constructed.
	 */
	boolean isPropertyPresent(@Nonnull K key);
}
