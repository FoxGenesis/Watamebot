package net.foxgenesis.property2.impl;

import net.foxgenesis.property2.async.AsyncProperty;
import net.foxgenesis.property2.async.AsyncPropertyResolver;

public class AsyncPropertyProvider<L> extends APropertyProvider<L, AsyncProperty<L>, AsyncPropertyResolver<L>> {

	public AsyncPropertyProvider(AsyncPropertyResolver<L> resolver) { super(resolver); }

	@Override
	public AsyncProperty<L> getProperty(String key) {
		return properties.computeIfAbsent(key, k -> new AsyncPropertyImpl<>(key, resolver));
	}
}
