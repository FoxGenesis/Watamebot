package net.foxgenesis.watame;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.util.resource.ModuleResource;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatameBotSettings {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	@NotNull
	public final Path configurationPath;

	private final INIConfiguration config;

	/**
	 * Authentication token (passwords should be stored as char[])
	 */
	private final char[] token;

	WatameBotSettings(@NotNull Path configPath) throws Exception {
		this(configPath, null);
	}

	WatameBotSettings(@NotNull Path configPath, @Nullable Path tokenFile2) throws Exception {
		configurationPath = Objects.requireNonNull(configPath);

		if (!isValidDirectory(configPath))
			throw new IOException("Invalid configuration directory");

		config = ResourceUtils.loadINI(new ModuleResource(getClass().getModule(), "/META-INF/defaults/watamebot.ini"),
				configurationPath, "watamebot.ini");

		// Get the token file
		Path tokenFile = tokenFile2 == null ? Path.of(config.getString("Token.tokenFile", "token.txt"))
				: configPath.resolve(tokenFile2);

		if (!isValidFile(tokenFile))
			throw new SettingsException("Invalid token file");

		// Read token
		token = readToken(tokenFile);
	}

	public ImmutableConfiguration getConfiguration() {
		return config;
	}

	char[] getToken() {
		return token;
	}

	/**
	 * NEED_JAVADOC
	 *
	 * @return
	 *
	 * @throws Throwable
	 */
	private static char[] readToken(Path filepath) throws Exception {
		char[] buffer = new char[70];
		int read = 0;
		try (BufferedReader reader = Files.newBufferedReader(filepath)) {
			read = reader.read(buffer);
		}
		logger.info("Checking token");
		if (read != 70)
			ExitCode.INVALID_TOKEN.programExit("Invalid token");
		return buffer;
	}

	private static boolean isValidDirectory(Path path) throws SettingsException {
		if (Files.notExists(path))
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
			}

		if (Files.exists(path) && check(Files.isDirectory(path), "Configuration path must be a directory!")) {
			return check(Files.isReadable(path), "Unable to read from configuration directory!")
					&& check(Files.isWritable(path), "Unable to write to configuration directory!");
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
