package net.foxgenesis.watame.util;

import java.util.ArrayList;
import java.util.List;

import net.foxgenesis.util.StringUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

/**
 * Utility class containing common methods for Discord.
 */
public final class DiscordUtils {
	/**
	 * Default avatar image in base 64.
	 * 
	 * @see #DEFAULT_AVATAR_URL
	 */
	public static final String DEFAULT_AVATAR = "data:image/png;base64,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";
	/**
	 * Default avatar image URL.
	 * 
	 * @see #DEFAULT_AVATAR
	 */
	public static final String DEFAULT_AVATAR_URL = "https://cdn.discordapp.com/embed/avatars/index.png";

	/**
	 * Get all attachments from a {@link Message}.
	 *
	 * @param message - message to check
	 *
	 * @return Returns a {@link List} of {@link AttachmentData}
	 */
	public static List<AttachmentData> getAttachments(Message message) {
		List<AttachmentData> attachments = new ArrayList<>();
		message.getAttachments().forEach(a -> attachments.add(new AttachmentData(message, a)));
		StringUtils.findURLs(message.getContentRaw()).forEach(u -> attachments.add(new AttachmentData(message, u)));
		return attachments;
	}

	/**
	 * Get a link to a {@link User}'s profile via the discord protocol.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "discord://-/users/%18d".formatted(member.getIdLong())
	 * </pre>
	 *
	 * @param member - member to get profile of
	 *
	 * @return Returns a URL using the discord protocol pointing to the specified
	 *         member's profile
	 */
	public static String getProfileProtocolLink(User member) {
		return getProfileProtocolLink(member.getIdLong());
	}

	/**
	 * Get a link to a member's profile via the discord protocol.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "discord://-/users/%18d".formatted(memberID)
	 * </pre>
	 *
	 * @param memberID - member id
	 *
	 * @return Returns a URL using the discord protocol pointing to the specified
	 *         member's profile
	 */
	public static String getProfileProtocolLink(long memberID) {
		return "discord://-/users/%18d".formatted(memberID);
	}

	/**
	 * Get a link to a {@link Guild} via the discord protocol.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "discord://-/channels/%18d".formatted(guild.getIdLong())
	 * </pre>
	 * 
	 * @param guild - guild to get link for
	 * 
	 * @return Returns a URl using the discord protocol pointing to the specified
	 *         guild
	 */
	public static String getGuildProtocolLink(Guild guild) {
		return "discord://-/channels/%18d".formatted(guild.getIdLong());
	}

	/**
	 * Mention a channel via its id.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "&lt;#%18d&gt;".formatted(channelID)
	 * </pre>
	 *
	 * @param channelID - channel id
	 *
	 * @return Returns a mention to the specified channel
	 */
	public static String mentionChannel(long channelID) {
		return "<#%18d>".formatted(channelID);
	}

	/**
	 * Mention a user via it's id.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "&lt;@%18d&gt;".formatted(userID)
	 * </pre>
	 *
	 * @param userID - user id
	 *
	 * @return Returns a mention to the specified user
	 */
	public static String mentionUser(long userID) {
		return "<@%18d>".formatted(userID);
	}

	/**
	 * Mention a role via its id.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "&lt;@&%18d&gt;".formatted(roleID)
	 * </pre>
	 *
	 * @param roleID - role id
	 *
	 * @return Returns a mention to the specified role
	 */
	public static String mentionRole(long roleID) {
		return "<@&%18d>".formatted(roleID);
	}

	/**
	 * Mention a slash command via its name and id.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "&lt;/%s:%18d&gt;".formatted(name, commandID)
	 * </pre>
	 *
	 * @param name      - command name
	 * @param commandID - command id
	 *
	 * @return Returns a mention to the specified slash command
	 */
	public static String mentionSlashCommand(String name, long commandID) {
		return "</%s:%18d>".formatted(name, commandID);
	}

	/**
	 * Mention a slash command via its name, sub command and id.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "&lt;/%s:%18d&gt;".formatted(command + " " + subCommand, commandID)
	 * </pre>
	 *
	 * @param command    - command name
	 * @param subCommand - sub command name
	 * @param commandID  - command id
	 *
	 * @return Returns a mention to the specified slash command
	 */
	public static String mentionSlashCommand(String command, String subCommand, long commandID) {
		return mentionSlashCommand(command + " " + subCommand, commandID);
	}

	/**
	 * Mention a slash command via its name, group, sub command and id.
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 *
	 * <pre>
	 * "&lt;/%s:%18d&gt;".formatted(command + " " + group + " " + subCommand, commandID)
	 * </pre>
	 *
	 * @param command    - command name
	 * @param group      - command group
	 * @param subCommand - sub command
	 * @param commandID  - command id
	 *
	 * @return Returns a mention to the specified slash command
	 */
	public static String mentionSlashCommand(String command, String group, String subCommand, long commandID) {
		return mentionSlashCommand(command + " " + group, subCommand, commandID);
	}
}
