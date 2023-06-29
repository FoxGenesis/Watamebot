package net.foxgenesis.watame.property;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.NewsChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.foxgenesis.property.IPropertyMapping;

public interface IGuildPropertyMapping extends IPropertyMapping {

	@Nullable
	public Category getAsCategory();

	@Nullable
	public Channel getAsChannel();
	
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
