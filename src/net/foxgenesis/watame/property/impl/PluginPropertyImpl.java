package net.foxgenesis.watame.property.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.lck.LCKPropertyResolver;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyMapping;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Guild;

public class PluginPropertyImpl implements PluginProperty {

	private final PropertyInfo info;

	protected final LCKPropertyResolver resolver;

	public PluginPropertyImpl(@NotNull PropertyInfo info, @NotNull LCKPropertyResolver resolver) {
		this.info = Objects.requireNonNull(info);
		this.resolver = Objects.requireNonNull(resolver);
	}

	@SuppressWarnings("null")
	@Override
	public Optional<PluginPropertyMapping> get(Guild lookup) {
		return resolver.getInternal(lookup.getIdLong(), getInfo()).map(t -> {
			try {
				return new PluginPropertyMapping(lookup, t, getInfo().type());
			} catch (IOException | SQLException e) {
				throw new PropertyException(e);
			}
		});
	}

	@Override
	public boolean set(Guild lookup, InputStream in) {
		return resolver.putInternal(lookup.getIdLong(), getInfo(), in);
	}

	@Override
	public boolean remove(Guild lookup) {
		return resolver.removeInternal(lookup.getIdLong(), getInfo());
	}

	@Override
	public boolean isPresent(Guild lookup) {
		return resolver.isPresent(lookup.getIdLong(), getInfo());
	}

	@Override
	public PropertyInfo getInfo() {
		return info;
	}

	@Override
	public int hashCode() {
		return Objects.hash(info, resolver);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PluginPropertyImpl other = (PluginPropertyImpl) obj;
		return Objects.equals(info, other.info) && Objects.equals(resolver, other.resolver);
	}

	@Override
	public String toString() {
		return "PluginPropertyImpl [" + (info != null ? "info=" + info + ", " : "")
				+ (resolver != null ? "resolver=" + resolver : "") + "]";
	}

}
