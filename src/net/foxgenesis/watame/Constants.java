package net.foxgenesis.watame;

import java.io.File;
import java.net.URL;

import javax.annotation.Nonnull;

public final class Constants {
	public static final String[] VALID_DISCORD_DOMAINS = { "discordapp.com", "discordapp.net", "discord.com",
			"discord.new", "discord.gift", "discord.gifts", "discord.media", "discord.gg", "discord.co", "discord.app",
			"dis.gd" };

	public static final URL DATABASE_SETUP_FILE = resource("CreateDatabase.sql");
	public static final URL DATABASE_OPERATIONS_FILE = resource("sql statements.kvp");

	public static final File pluginFolder = new File("plugins");

	private static final URL resource(@Nonnull String path) {
		return Constants.class.getResource("/main/resources/" + path);
	}
}
