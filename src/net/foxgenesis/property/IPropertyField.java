package net.foxgenesis.property;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An interface that provides generic methods used to retrieve and set
 * properties from various data structures.
 * 
 * @author Ashley
 *
 * @param <K> Key type that points to a property
 * @param <F> Lookup data type that coincides property lookup
 * @param <M> Property mapping type that turns the raw property data into a
 *            usable type
 * @see IPropertyProvider
 * @see IPropertyMapping
 */
public interface IPropertyField<K, F, M extends IPropertyMapping> {

	/**
	 * Get this property's data wrapped in an {@link IPropertyMapping}.
	 * 
	 * @param from - property lookup data
	 * @return The property's data inside an {@link IPropertyMapping}
	 */
	@CheckForNull
	M get(@Nonnull F from);

	/**
	 * Get this property's value by applying the mapping into the {@code resolver}
	 * function or returning the fallback suppliers value if the {@code resolver}'s
	 * result is {@code null}.
	 * 
	 * @param <U>      - returned data type of the {@code resolver}
	 * @param from     - property lookup data
	 * @param fallback - supplier providing a fallback value
	 * @param resolver - function to resolve the mapping into a usable data type
	 * @return Returns the property's value by applying it using the
	 *         {@code resolver} function. If the result of the {@code resolver}
	 *         function is {@code null}, the {@code fallback} supplier's value will
	 *         be returned instead
	 */
	default <U> U get(@Nonnull F from, @Nonnull Supplier<U> fallback,
			@Nonnull Function<? super M, ? extends U> resolver) {
		Objects.requireNonNull(fallback);
		Objects.requireNonNull(resolver);

		M mapping = get(from);
		if (mapping != null)
			return resolver.apply(mapping);
		return fallback.get();
	}

	/**
	 * Get this property's value by applying the mapping into the {@code resolver}
	 * function or returning the fallback value if the {@code resolver}'s result is
	 * {@code null}.
	 * 
	 * @param <U>      - returned data type of the {@code resolver}
	 * @param from     - property lookup data
	 * @param fallback - fallback value
	 * @param resolver - function to resolve the mapping into a usable data type
	 * @return Returns the property's value by applying it using the
	 *         {@code resolver} function. If the result of the {@code resolver}
	 *         function is {@code null}, the {@code fallback} value will be returned
	 *         instead
	 */
	default <U> U get(@Nonnull F from, @Nullable U fallback, @Nonnull Function<? super M, ? extends U> resolver) {
		return get(from, () -> fallback, resolver);
	}

	/**
	 * Get this property's value by applying the mapping into the {@code resolver}
	 * function.
	 * 
	 * @param <U>      - returned data type of the {@code resolver}
	 * @param from     - property lookup data
	 * @param resolver - function to resolve the mapping into a usable data type
	 * @return Returns the property's value by applying the mapping into the
	 *         {@code resolver} function.
	 */
	default <U> U get(@Nonnull F from, @Nonnull Function<? super M, U> resolver) {
		return get(from, () -> null, resolver);
	}

	/**
	 * Update/Set this property's value.
	 * 
	 * @param from  - property lookup data
	 * @param value - the new value
	 * @return Returns {@code true} if the property was updated successfully
	 * @see #set(Object, Object, boolean)
	 */
	boolean set(@Nonnull F from, @Nullable Object value);

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
	default boolean set(@Nonnull F from, @Nullable Object value, boolean userEdited) {
		if (userEdited && !isEditable())
			throw new UnmodifiablePropertyException(this, this + " is not editable!");
		return set(from, value);
	}

	/**
	 * Get the key pointing to the property.
	 * 
	 * @return The property's key
	 */
	@Nonnull
	K getKey();

	/**
	 * Check if this property is present.
	 * 
	 * @param from - lookup data
	 * @return Returns {@code true} if the property was found using the provided
	 *         lookup data
	 */
	boolean isPresent(@Nonnull F from);

	/**
	 * Check if this property is end user editable.
	 * 
	 * @return Returns {@code true} if this property is allowed to be modified by
	 *         the end user
	 */
	boolean isEditable();
}
