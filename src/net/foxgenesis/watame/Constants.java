package net.foxgenesis.watame;

import java.net.URL;

public final class Constants {
	public static final URL DATABASE_SETUP_FILE = Constants.class.getResource("/main/resources/CreateDatabase.sql");
	
	public static final String[] VALID_DISCORD_DOMAINS = { "discordapp.com", "discordapp.net", "discord.com", "discord.new",
			"discord.gift", "discord.gifts", "discord.media", "discord.gg", "discord.co", "discord.app", "dis.gd" };
}
