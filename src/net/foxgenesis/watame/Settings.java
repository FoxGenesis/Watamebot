package net.foxgenesis.watame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import net.foxgenesis.util.PushBullet;
import net.foxgenesis.util.resource.ModuleResource;
import net.foxgenesis.util.resource.ResourceUtils;

public class Settings {

	private final Path configPath;
	private final Path tokenFile;
	private final PushBullet pb;
	private final Configuration config;

	public Settings(SettingsBuilder builder) throws IOException, ConfigurationException {
		this.configPath = Objects.requireNonNull(builder.configPath);

		// Load config file
		this.config = ResourceUtils.loadINI(
				new ModuleResource(getClass().getModule(), "/META-INF/defaults/watamebot.ini"), configPath,
				"watamebot.ini");

		// Get token file
		this.tokenFile = configPath.resolve(builder.tokenFile != null ? builder.tokenFile
				: config.getString("WatameBot.Token.tokenFile", "token.txt"));

		// Setup push bullet
		String tmp = config.getString("PushBullet.token", builder.pbToken);
		tmp = !(tmp == null || tmp.isBlank()) ? tmp.trim() : null;
		this.pb = new PushBullet(tmp);

		// Set our log level
		System.setProperty("LOG_LEVEL", builder.logLevel != null ? builder.logLevel.name().toLowerCase()
				: config.getString("Logging.logLevel", "info"));
	}

	public Path getConfigPath() {
		return configPath;
	}

	public Path tokenFile() {
		return tokenFile;
	}

	public Configuration getConfiguration() {
		return config;
	}

	public PushBullet getPushbullet() {
		return pb;
	}

	public String getToken() {
		return readToken(tokenFile);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param filepath
	 *
	 * @return Returns the read token
	 *
	 */
	private static String readToken(Path filepath) {
		String token = null;
		try {
			// Create configuration file
			if (Files.notExists(filepath, LinkOption.NOFOLLOW_LINKS))
				Files.createFile(filepath);
			else if (Files.isDirectory(filepath, LinkOption.NOFOLLOW_LINKS))
				throw new IOException(filepath.toString() + " is not a regular file!");
			else if (!Files.isReadable(filepath))
				throw new IOException("Unable to read " + filepath.toString() + ". Missing permissions!");
			else
				token = Files.lines(filepath).filter(s -> !s.startsWith("#")).map(String::trim).findFirst().orElse("");
		} catch (IOException e) {
			ExitCode.NO_TOKEN.programExitSilent("Failed to get token", e);
		}
		if(token == null || token.isBlank())
			ExitCode.NO_TOKEN.programExitSilent("Token must not be blank");
		return token;
	}

	public static enum LogLevel {
		TRACE, DEBUG, INFO, ERROR
	}
}
