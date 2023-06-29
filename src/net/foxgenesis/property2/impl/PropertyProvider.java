package net.foxgenesis.property2.impl;

import org.jetbrains.annotations.NotNull;

import net.foxgenesis.property2.Property;
import net.foxgenesis.property2.PropertyResolver;

public class PropertyProvider<L> extends APropertyProvider<L, Property<L>, PropertyResolver<L>> {

	public PropertyProvider(@NotNull PropertyResolver<L> resolver) {
		super(resolver);
	}

	@Override
	@NotNull
	public Property<L> getProperty(@NotNull String key) {
		return properties.computeIfAbsent(key, k -> new PropertyImpl<>(key, resolver));
	}
}
