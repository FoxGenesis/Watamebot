package net.foxgenesis.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IProperty<K, L, M extends IPropertyMapping> extends ImmutableProperty<K, L, M> {

	/**
	 * Update/Set this property's value.
	 * 
	 * @param from  - property lookup data
	 * @param value - the new value
	 * @return Returns {@code true} if the property was updated successfully
	 * @see #set(Object, Object, boolean)
	 */
	boolean set(@Nonnull L from, @Nullable Object value);

	/**
	 * Update/Set this property's value.
	 * 
	 * @param from       - property lookup data
	 * @param value      - the new value
	 * @param userEdited - was this change initiated by the end user
	 * @return Returns {@code true} if the property was updated successfully
	 * @throws UnmodifiablePropertyException Thrown if this property is not editable
	 *                                       and {@code userEdited} is {@code true}
	 * @see #set(Object, Object)
	 */
	default boolean set(@Nonnull L from, @Nullable Object value, boolean userEdited) {
		if (userEdited && !isEditable())
			throw new UnmodifiablePropertyException(this, this + " is not editable!");
		return set(from, value);
	}
}
