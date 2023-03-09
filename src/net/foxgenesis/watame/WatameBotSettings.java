package net.foxgenesis.watame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.util.resource.ModuleResource;

public class WatameBotSettings {

	private static final Logger logger = LoggerFactory.getLogger(WatameBotSettings.class);
	
	@Nonnull
	public final Path configurationPath;

	private final PropertiesConfiguration config;

	private final String token;
	
	WatameBotSettings(@Nonnull Path configPath) throws Exception {
		this(configPath, null);
	}

	WatameBotSettings(@Nonnull Path configPath, @Nullable Path tokenFile2) throws Exception {
		this.configurationPath = Objects.requireNonNull(configPath);

		if (isValidDirectory(configPath)) {
			config = ResourceUtils.loadConfig(new ModuleResource(getClass().getModule(), "/defaults/watamebot.ini"),
					this.configurationPath, "watamebot.ini");
		} else
			throw new IOException("Invalid configuration directory");

		// Get the token
		Path tokenFile = tokenFile2 == null ? Path.of(config.getString("tokenFile", "token.txt")) : configPath.resolve(tokenFile2);
		
		if (isValidFile(tokenFile)) {
			token = readToken(tokenFile);
		} else
			throw new SettingsException("Invalid token file");

		// Validate the token
		logger.info("Checking token");
		if (!isValidToken(token)) {
			ExitCode.INVALID_TOKEN.programExit("Invalid token");
		}
	}

	public ImmutableConfiguration getConfiguration() { return config; }
	
	String getToken() {
		return token;
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @return
	 * @throws Throwable 
	 */
	private static String readToken(Path filepath) throws Exception {
		return Files.lines(filepath).filter(s -> !s.startsWith("#")).map(String::trim).findFirst().orElse("");
	}

	/**
	 * Check the discord token is valid.
	 * 
	 * @return Returns {@code true} if the token is not {@code null} and is not
	 *         blank
	 * @throws Throwable 
	 */
	private static boolean isValidToken(String token) throws Exception {
		return !(token == null || token.isBlank());
	}

	private static boolean isValidDirectory(Path path) throws SettingsException {
		if (Files.notExists(path))
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
			}

		if (Files.exists(path)) {
			if (check(Files.isDirectory(path), "Configuration path must be a directory!")) {
				return check(Files.isReadable(path), "Unable to read from configuration directory!")
						&& check(Files.isWritable(path), "Unable to write to configuration directory!");
			}
		}

		return false;
	}

	private static boolean isValidFile(Path path) throws SettingsException {
		if (Files.notExists(path))
			try {
				Files.createFile(path);
			} catch (IOException e) {
				e.printStackTrace();
			}

		if (Files.exists(path))
			if (check(Files.isRegularFile(path), "Token file must be a regular file!"))
				return check(Files.isReadable(path), "Unable to read from token file!");

		return false;
	}

	private static boolean check(boolean toTest, String err) throws SettingsException {
		if (toTest)
			return true;
		throw new SettingsException(err);
	}
	
	public static class SettingsException extends Exception {

		private static final long serialVersionUID = 5228408925488573319L;
		
		public SettingsException(String msg) {
			super(msg);
		}
	}
}
