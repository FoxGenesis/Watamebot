package net.foxgenesis.watame;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import net.foxgenesis.util.SingleInstanceUtil;
import net.foxgenesis.watame.Settings.LogLevel;

/**
 * Program main class.
 *
 * @author Ashley
 */
public class Main {

	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	private static Settings settings;

	/**
	 * Program entry point.
	 *
	 * @param args - program arguments
	 *
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("watame.status", "START-UP");

		SettingsBuilder builder = new SettingsBuilder();
		builder.setConfigPath(Path.of("config"));

		int length = args.length;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg.toLowerCase()) {

			case "-config" -> {
				if (hasArg(i, length, "-config")) {
					i++;
					builder.setConfigPath(Path.of(StringUtils.strip(args[i], "\"")));
				}
			}

			case "-loglevel" -> {
				if (hasArg(i, length, "-loglevel")) {
					i++;
					String tmp = args[i];
					if (StringUtils.equalsAnyIgnoreCase(tmp, "info", "debug", "trace")) {
						LogLevel level = LogLevel.valueOf(tmp.toUpperCase());

						builder.setLogLevel(level);
						logger.info("Setting logging level to: " + level);
					}
				}
			}

			case "-tokenfile" -> {
				if (hasArg(i, length, "-tokenfile")) {
					i++;
					builder.setTokenFile(StringUtils.strip(args[i], "\""));
				}
			}

			case "-pbToken" -> {
				if (hasArg(i, length, "-pbToken")) {
					i++;
					builder.setPushbulletToken(args[i]);
				}
			}
			}
		}

		// Load settings
		settings = new Settings(builder);
		ImmutableConfiguration config = settings.getConfiguration();

		try {
			// Enable ANSI console
			if (config.getBoolean("Logging.ansiConsole", true)) {
				logger.info("Installing ANSI console");
				AnsiConsole.systemInstall();
			}

			restartLogging();

			// Attempt to obtain instance lock
			if (config.getBoolean("Startup.SingleInstance.enabled", true))
				getLock(config.getInt("Startup.SingleInstance.retries", 5));

			// Print out our current multi-threading information
			logger.info("Checking multi-threading");
			logger.info("CPU Cores: {}  |  Parallelism: {}  |  CommonPool Common Parallelism: {}",
					Runtime.getRuntime().availableProcessors(), ForkJoinPool.commonPool().getParallelism(),
					ForkJoinPool.getCommonPoolParallelism());

			// First call of WatameBot class. Will cause instance creation
			WatameBot watame = WatameBot.INSTANCE;

			System.out.println();

			watame.start();
		} catch (Exception e) {
			settings.getPushbullet().pushPBMessage("An Error Occurred in Watame", ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}

	private static boolean hasArg(int index, int length, String argName) {
		if (index + 1 > length) {
			logger.error("Missing argument for ", argName);
			System.exit(1);
			return false;
		}

		return true;
	}

	private static boolean getLock(int retries) throws Exception {
		try {
			SingleInstanceUtil.waitAndGetLock(retries);
			return true;
		} catch (SingleInstanceUtil.SingleInstanceLockException e) {
			// Another instance is already running
			ExitCode.INSTANCE_ALREADY_RUNNING.programExit("Another instance is already running! Exiting...");
			return false;
		}
	}

	private static void restartLogging() {
		// assume SLF4J is bound to logback in the current environment
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			// Call context.reset() to clear any previous configuration, e.g. default
			// configuration. For multi-step configuration, omit calling context.reset().
			context.reset();
			try (InputStream in = Constants.LOGGING_SETTINGS.openStream()) {
				configurator.doConfigure(in);
			}
		} catch (IOException | JoranException e) {

		}
	}

	@NotNull
	static Settings getSettings() {
		return settings;
	}
}
