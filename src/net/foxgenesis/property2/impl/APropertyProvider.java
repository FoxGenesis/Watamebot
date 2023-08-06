package net.foxgenesis.property2.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import net.foxgenesis.property2.Property;
import net.foxgenesis.property2.PropertyResolver;
import net.foxgenesis.property2.PropertyType;

public abstract class APropertyProvider<L, T extends Property<L>, R extends PropertyResolver<L>> {

	protected final R resolver;
	protected final HashMap<String, T> properties = new HashMap<>();

	public APropertyProvider(@NotNull R resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@NotNull
	public abstract T getProperty(@NotNull String key);

	public boolean isPropertyPresent(@NotNull String key) {
		return properties.containsKey(key);
	}

	@NotNull
	public Set<String> keySet() {
		return Collections.unmodifiableSet(properties.keySet());
	}

	@NotNull
	public Stream<T> typesOf(@NotNull L lookup, @NotNull PropertyType type) {
		return Collections.unmodifiableCollection(properties.values()).stream()
				.filter(p -> p != null && p.getType(lookup).equals(type));
	}
}
