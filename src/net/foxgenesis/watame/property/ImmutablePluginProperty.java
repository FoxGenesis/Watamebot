package net.foxgenesis.watame.property;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.PropertyMapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Guild;

public interface ImmutablePluginProperty {
	/**
	 * Get the current value of this property.
	 * 
	 * @param guild - guild to get property of
	 * 
	 * @return Returns a {@link PropertyMapping} containing the raw data retrieved
	 */
	@NotNull
	Optional<PluginPropertyMapping> get(@NotNull Guild guild);

	/**
	 * Get the current value of this property and map it with the specified
	 * {@code mapper}.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 * <blockquote>
	 * 
	 * <pre>
	 * get(guild, null, mapper)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param <U>    Return type
	 * @param guild  - guild to get property of
	 * @param mapper - function to convert the raw data into a usable type
	 * 
	 * @return Returns the mapped data or {@code null} if this property is empty
	 */
	@Nullable
	default <U> U get(@NotNull Guild guild, @NotNull Function<? super PluginPropertyMapping, U> mapper) {
		return get(guild, null, mapper);
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
	 * get(guild).map(mapper).orElseGet(defaultValue != null ? defaultValue : () -> null);
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param <U>          Return type
	 * @param guild        - guild to get property of
	 * @param defaultValue - default value supplier
	 * @param mapper       - function to convert the raw data into a usable type
	 * 
	 * @return Returns the mapped data or {@code defaultValue} if this property is
	 *         empty
	 */
	@Nullable
	default <U> U get(@NotNull Guild guild, @Nullable Supplier<U> defaultValue,
			@NotNull Function<? super PluginPropertyMapping, U> mapper) {
		Optional<PluginPropertyMapping> m = get(guild);
		if (m.isEmpty())
			return defaultValue != null ? defaultValue.get() : null;
		return mapper.apply(m.get());
	}

	/**
	 * Check if this property is populated in the configuration.
	 * 
	 * @param guild - guild to get property of
	 * 
	 * @return Returns {@code true} if the {@code guild} with this
	 *         {@link PropertyInfo} was found inside the configuration (empty or
	 *         not)
	 */
	boolean isPresent(@NotNull Guild guild);

	/**
	 * Get the definition of this property
	 * 
	 * @return Returns the {@link PropertyInfo} linked to this property
	 */
	@NotNull
	PropertyInfo getInfo();
}
