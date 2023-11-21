package net.foxgenesis.watame.property;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

import net.foxgenesis.property.PropertyType;
import net.foxgenesis.property.lck.impl.BlobMapping;
import net.foxgenesis.watame.WatameBot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

public class PluginPropertyMapping extends BlobMapping {

	public PluginPropertyMapping(long lookup, byte[] data, @NotNull PropertyType type) {
		super(lookup, data, type);
	}

	public PluginPropertyMapping(@NotNull Guild guild, byte[] data, @NotNull PropertyType type) {
		super(guild.getIdLong(), data, type);
	}

	@SuppressWarnings("exports")
	public PluginPropertyMapping(long lookup, @NotNull Blob blob, @NotNull PropertyType type)
			throws IOException, SQLException {
		super(lookup, blob, type);
	}

	@SuppressWarnings("exports")
	public PluginPropertyMapping(@NotNull Guild guild, @NotNull Blob blob, @NotNull PropertyType type)
			throws IOException, SQLException {
		super(guild.getIdLong(), blob, type);
	}

	@Nullable
	public Role getAsRole() {
		return getGuild().getRoleById(getAsLong());
	}

	public Role[] getAsRoleArray() {
		long[] arr = getAsLongArray();
		Role[] out = new Role[arr.length];
		for (int i = 0; i < arr.length; i++)
			out[i] = getGuild().getRoleById(arr[i]);
		return out;
	}

	@Nullable
	public GuildMessageChannel getAsMessageChannel() {
		return getGuild().getChannelById(GuildMessageChannel.class, getAsLong());
	}

	public CacheRestAction<Member> retrieveAsMember() {
		return getGuild().retrieveMemberById(getAsLong());
	}

	@Nullable
	public Member getAsMember() {
		return getGuild().getMemberById(getAsLong());
	}

	public Guild getGuild() {
		// Hard coded to reduce work on end user
		return WatameBot.getJDA().getGuildById(getLookup());
	}
}
