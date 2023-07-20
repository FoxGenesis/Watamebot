package net.foxgenesis.watame.property;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.foxgenesis.property.IPropertyMapping;

public interface IGuildPropertyMapping extends IPropertyMapping {

	@Nullable
	public Category getAsCategory();

	@Nullable
	public Channel getAsChannel();

	@Nullable
	public GuildMessageChannel getAsMessageChannel();

	@Nullable
	public TextChannel getAsTextChannel();

	@Nullable
	public VoiceChannel getAsVoiceChannel();

	@Nullable
	public StageChannel getAsStageChannel();

	@Nullable
	public NewsChannel getAsNewsChannel();

	@Nullable
	public Role getAsRole();

	@Nullable
	public Member getAsMember();
}
