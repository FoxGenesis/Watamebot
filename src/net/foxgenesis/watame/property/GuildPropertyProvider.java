package net.foxgenesis.watame.property;

import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.property.IPropertyField;
import net.foxgenesis.property.IPropertyProvider;
import net.foxgenesis.watame.sql.IGuildDataProvider;

@Deprecated(forRemoval = true)
public class GuildPropertyProvider implements IPropertyProvider<String, Guild, IGuildPropertyMapping> {

	private final IGuildDataProvider database;
	private final HashMap<String, IPropertyField<String, Guild, IGuildPropertyMapping>> properties = new HashMap<>();

	public GuildPropertyProvider(IGuildDataProvider database) { this.database = Objects.requireNonNull(database); }

	@Override
	public IPropertyField<String, Guild, IGuildPropertyMapping> getProperty(@Nonnull String key) {
		return properties.computeIfAbsent(key, k -> new GuildProperty(k, database));
	}

	@Override
	public boolean isPropertyPresent(@Nonnull String key) { return properties.containsKey(key); }

	@Override
	public String toString() {
		return "GuildPropertyProvider [database=" + database + ", properties=" + properties + "]";
	}

	@Override
	public int hashCode() { return Objects.hash(database, properties); }

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuildPropertyProvider other = (GuildPropertyProvider) obj;
		return Objects.equals(database, other.database) && Objects.equals(properties, other.properties);
	}
}
