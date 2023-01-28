package net.foxgenesis.watame.property;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.config.fields.JSONObjectAdv;
import net.foxgenesis.property.IPropertyField;
import net.foxgenesis.watame.sql.IDatabaseManager;

public class GuildProperty implements IPropertyField<String, Guild, IGuildPropertyMapping> {

	private final String key;

	private final IDatabaseManager database;

	GuildProperty(@Nonnull String key, @Nonnull IDatabaseManager database) {
		this.key = Objects.requireNonNull(key, "Key must not be null!");
		this.database = Objects.requireNonNull(database, "Database must not be null!");
	}

	@Override
	@CheckForNull
	public IGuildPropertyMapping get(@Nonnull Guild from) {
		Objects.requireNonNull(from);

		JSONObject data = getData(from);

		if (data == null)
			throw new IllegalStateException("Data has not been recieved yet!");
		
		return isPresent(from) ? new GuildPropertyMapping(key, data, from) : null;
	}

	@Override
	public boolean set(Guild from, Object value) {
		getData(from).put(key, value);
		return true;
	}

	@Override
	@Nonnull
	public String getKey() { return key; }

	@Override
	public boolean isEditable() { return false; }

	@Override
	public boolean isPresent(Guild from) { return getData(from).has(key); }

	private JSONObjectAdv getData(Guild from) { return database.getDataForGuild(from).getConfig(); }

	@Override
	public int hashCode() { return Objects.hash(database, key); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuildProperty other = (GuildProperty) obj;
		return Objects.equals(database, other.database) && Objects.equals(key, other.key);
	}

	@Override
	public String toString() { return "GuildProperty [key=" + key + ", database=" + database + "]"; }
}