package net.foxgenesis.property2.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.foxgenesis.property2.Property;
import net.foxgenesis.property2.PropertyResolver;
import net.foxgenesis.property2.PropertyType;

public abstract class APropertyProvider<L, T extends Property<L>, R extends PropertyResolver<L>> {

	protected final R resolver;
	protected final HashMap<String, T> properties = new HashMap<>();

	public APropertyProvider(@Nonnull R resolver) { this.resolver = Objects.requireNonNull(resolver); }

	@Nonnull
	public abstract T getProperty(@Nonnull String key);

	public boolean isPropertyPresent(@Nonnull String key) { return properties.containsKey(key); }

	@Nonnull
	public Set<String> keySet() { return Collections.unmodifiableSet(properties.keySet()); }

	@Nonnull
	public Stream<T> typesOf(@Nonnull L lookup, @Nonnull PropertyType type) {
		return Collections.unmodifiableCollection(properties.values()).stream().filter(p -> p.getType(lookup).equals(type));
	}
}
