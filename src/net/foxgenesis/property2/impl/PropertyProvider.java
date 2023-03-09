package net.foxgenesis.property2.impl;

import javax.annotation.Nonnull;

import net.foxgenesis.property2.Property;
import net.foxgenesis.property2.PropertyResolver;

public class PropertyProvider<L> extends APropertyProvider<L, Property<L>, PropertyResolver<L>> {

	public PropertyProvider(@Nonnull PropertyResolver<L> resolver) { super(resolver); }

	@Override
	@Nonnull
	public Property<L> getProperty(@Nonnull String key) {
		return properties.computeIfAbsent(key, k -> new PropertyImpl<>(key, resolver));
	}
}
