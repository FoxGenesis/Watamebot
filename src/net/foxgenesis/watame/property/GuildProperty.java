package net.foxgenesis.watame.property;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.config.fields.JSONObjectAdv;
import net.foxgenesis.property.IProperty;
import net.foxgenesis.watame.sql.IGuildDataProvider;

public class GuildProperty implements IProperty<String, Guild, IGuildPropertyMapping> {

	private final String key;

	private final IGuildDataProvider database;

	public GuildProperty(String key, IGuildDataProvider database) {
		this.key = Objects.requireNonNull(key, "Key must not be null!");
		this.database = Objects.requireNonNull(database, "Database must not be null!");
	}

	@Override
	@Nullable
	public IGuildPropertyMapping get(@NotNull Guild from) {
		Objects.requireNonNull(from);

		JSONObject data = getData(from);

		if (data == null)
			throw new IllegalStateException("Data has not been recieved yet!");

		return isPresent(from) ? new GuildPropertyMapping(key, data, from) : null;
	}

	@SuppressWarnings({ "removal", "deprecation" })
	@Override
	public boolean set(Guild from, @Nullable Object value) {
		if (value != null)
			getData(from).put(key, value);
		else
			getData(from).remove(key);
		return true;
	}

	@Override
	@NotNull
	public String getKey() {
		return key;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isPresent(Guild from) {
		return getData(from).has(key);
	}

	@SuppressWarnings("removal")
	private JSONObjectAdv getData(Guild from) {
		return database.getDataForGuild(from).getConfig();
	}

	@Override
	public int hashCode() {
		return Objects.hash(database, key);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
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
	public String toString() {
		return "GuildProperty [key=" + key + ", database=" + database + "]";
	}
}