package net.foxgenesis.property;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ImutableProperty<L, M extends PropertyMapping> {
	/**
	 * Get the current value of this property.
	 *
	 * @param lookup - property lookup
	 *
	 * @return Returns a {@link PropertyMapping} containing the raw data retrieved
	 */
	@NotNull
	Optional<? extends M> get(@NotNull L lookup);

	/**
	 * Get the current value of this property and map it with the specified
	 * {@code mapper}.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 * <blockquote>
	 *
	 * <pre>
	 * get(lookup, null, mapper)
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @param <U>    Return type
	 * @param lookup - property lookup
	 * @param mapper - function to convert the raw data into a usable type
	 *
	 * @return Returns the mapped data or {@code null} if this property is empty
	 */
	@Nullable
	default <U> U get(@NotNull L lookup, @NotNull Function<? super M, U> mapper) {
		return get(lookup, null, mapper);
	}

	/**
	 * Get the current value of this property and map it with the specified
	 * {@code mapper}. If the property is empty, the {@code defaultValue} will be
	 * returned instead.
	 *
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 * <blockquote>
	 *
	 * <pre>
	 * get(lookup).map(mapper).orElseGet(defaultValue != null ? defaultValue : () -> null);
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @param <U>          Return type
	 * @param lookup       - property lookup
	 * @param defaultValue - default value supplier
	 * @param mapper       - function to convert the raw data into a usable type
	 *
	 * @return Returns the mapped data or {@code defaultValue} if this property is
	 *         empty
	 */
	@Nullable
	default <U> U get(@NotNull L lookup, @Nullable Supplier<U> defaultValue,
			@NotNull Function<? super M, U> mapper) {
		return get(lookup).map(mapper).orElseGet(defaultValue != null ? defaultValue : () -> null);
	}

	/**
	 * Check if this property is populated in the configuration.
	 *
	 * @param lookup - property lookup
	 *
	 * @return Returns {@code true} if the {@code lookup} with this
	 *         {@link PropertyInfo} was found inside the configuration (empty or
	 *         not)
	 */
	boolean isPresent(@NotNull L lookup);

	/**
	 * Get the definition of this property
	 *
	 * @return Returns the {@link PropertyInfo} linked to this property
	 */
	@NotNull
	PropertyInfo getInfo();
}
