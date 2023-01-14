package net.foxgenesis.watame;

import java.io.File;

import javax.annotation.Nonnull;

import net.foxgenesis.util.ResourceUtils.ModuleResource;

public final class Constants {
	public static final String[] VALID_DISCORD_DOMAINS = { "discordapp.com", "discordapp.net", "discord.com",
			"discord.new", "discord.gift", "discord.gifts", "discord.media", "discord.gg", "discord.co", "discord.app",
			"dis.gd" };

	public static final ModuleResource DATABASE_SETUP_FILE = resource("table setup.sql");
	public static final ModuleResource DATABASE_OPERATIONS_FILE = resource("sql statements.kvp");

	public static final File PLUGINS_FOLDER = new File("plugins");

	private static final ModuleResource resource(@Nonnull String path) {
		return new ModuleResource("watamebot", "/resources/" + path);
	}
}
