package net.foxgenesis.property;

import java.util.function.Consumer;

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
	 * @see #set(Object, Object, boolean, Consumer, Consumer)
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
	 * @see #set(Object, Object, boolean, Consumer, Consumer)
	 */
	default boolean set(@Nonnull L from, @Nullable Object value, boolean userEdited) {
		if (userEdited && !isEditable())
			throw new UnmodifiablePropertyException(this, this + " is not editable!");
		return set(from, value);
	}

	/**
	 * Update/Set this property's value.
	 * 
	 * @param from         - property lookup data
	 * @param value        - the new value
	 * @param userEdited   - was this change initiated by the end user
	 * @param onSet        - on set handler
	 * @param errorHandler - optional exception handler
	 * @see #set(Object, Object)
	 * @see #set(Object, Object, boolean)
	 */
	default void set(@Nonnull L from, @Nullable Object value, boolean userEdited, @Nonnull Consumer<Boolean> onSet,
			@Nullable Consumer<Throwable> errorHandler) {
		try {
			boolean wasSet = set(from, value, userEdited);
			onSet.accept(wasSet);
		} catch (Exception e) {
			if (errorHandler != null)
				errorHandler.accept(e);
			else
				e.printStackTrace();
		}
	}
}
