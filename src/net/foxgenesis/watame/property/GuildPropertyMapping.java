package net.foxgenesis.watame.property;

import java.util.Objects;

import javax.annotation.Nullable;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class GuildPropertyMapping implements IGuildPropertyMapping {

	private final String key;
	private final JSONObject data;
	private final Guild guild;

	public <T extends JSONObject> GuildPropertyMapping(String key, T data, Guild guild) {
		this.key = Objects.requireNonNull(key);
		this.data = Objects.requireNonNull(data);
		this.guild = Objects.requireNonNull(guild);
	}

	@Override
	public String getAsString() { return data.getString(key); }

	@Override
	public long getAsLong() { return data.getLong(key); }

	@Override
	public double getAsDouble() { return data.getDouble(key); }

	@Override
	public float getAsFloat() { return data.getFloat(key); }

	@Override
	public int getAsInt() { return data.getInt(key); }

	@Override
	public boolean getAsBoolean() { return data.getBoolean(key); }

	@Override
	@Nullable
	public Category getAsCategory() { return guild.getCategoryById(getAsLong()); }

	@Override
	@Nullable
	public Channel getAsChannel() { return guild.getGuildChannelById(getAsLong()); }

	@Override
	@Nullable
	public TextChannel getAsTextChannel() { return guild.getTextChannelById(getAsLong()); }

	@Override
	@Nullable
	public VoiceChannel getAsVoiceChannel() { return guild.getVoiceChannelById(getAsLong()); }

	@Override
	@Nullable
	public StageChannel getAsStageChannel() { return guild.getStageChannelById(getAsLong()); }

	@Override
	@Nullable
	public NewsChannel getAsNewsChannel() { return guild.getNewsChannelById(getAsLong()); }

	@Override
	@Nullable
	public Role getAsRole() { return guild.getRoleById(getAsLong()); }

	@Override
	@Nullable
	public Member getAsMember() { return guild.getMemberById(getAsLong()); }

	@Override
	public int hashCode() { return Objects.hash(data, guild, key); }

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuildPropertyMapping other = (GuildPropertyMapping) obj;
		return Objects.equals(key, other.key) && Objects.equals(guild, other.guild) && Objects.equals(data, other.data);
	}

	@Override
	public String toString() {
		return "GuildPropertyMapping [key=" + key + ", data=" + data + ", guild=" + guild + "]";
	}
}
