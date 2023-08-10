package net.foxgenesis.watame.property;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Optional;

import net.foxgenesis.property.PropertyException;
import net.foxgenesis.property.PropertyInfo;
import net.foxgenesis.property.impl.LCKProperty;
import net.foxgenesis.property.impl.LCKPropertyResolver;

import net.dv8tion.jda.api.entities.Guild;

public class PluginPropertyImpl extends LCKProperty implements PluginProperty {

	@SuppressWarnings("exports")
	public PluginPropertyImpl(PropertyInfo info, LCKPropertyResolver resolver) {
		super(info, resolver);
	}

	@Override
	public Optional<PluginPropertyMapping> get(Guild guild) {
		return get(guild.getIdLong());
	}

	@SuppressWarnings("null")
	@Override
	public Optional<PluginPropertyMapping> get(long lookup) {
		return resolver.getInternal(lookup, getInfo()).map(t -> {
			try {
				return new PluginPropertyMapping(lookup, t, getInfo().type());
			} catch (IOException | SQLException e) {
				throw new PropertyException(e);
			}
		});
	}

	@Override
	public boolean set(Guild lookup, Serializable obj) {
		return set(lookup.getIdLong(), obj);
	}

	@Override
	public boolean set(Guild lookup, byte[] data) {
		return set(lookup.getIdLong(), new ByteArrayInputStream(data));
	}

	@Override
	public boolean set(Guild lookup, InputStream in) {
		return set(lookup.getIdLong(), in);
	}

	@Override
	public boolean remove(Guild lookup) {
		return remove(lookup.getIdLong());
	}

	@Override
	public boolean isPresent(Guild lookup) {
		return isPresent(lookup.getIdLong());
	}
}
