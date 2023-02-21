package net.foxgenesis.watame.util;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * Utility class for Discord related things.
 * 
 * @author Ashley
 *
 */
public final class DiscordUtils {

	/**
	 * NEED_JAVADOC
	 * 
	 * @param guild
	 * @return
	 */
	public static Member getBotMember(@Nonnull Guild guild) {
		return guild.getSelfMember();
	}

	public static int getBotColor(@Nonnull Guild guild) {
		return getBotMember(guild).getColorRaw();
	}
}
