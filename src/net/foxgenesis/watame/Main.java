package net.foxgenesis.watame;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import net.foxgenesis.util.ProgramArguments;
import net.foxgenesis.util.SingleInstanceUtil;
import net.foxgenesis.util.resource.ModuleResource;

/**
 * Program main class.
 *
 * @author Ashley
 */
public class Main {

	private final static Logger logger = LoggerFactory.getLogger(Main.class);
	/**
	 * Program arguments
	 */
	private static ProgramArguments params;

	private static WatameBotSettings settings;

	/**
	 * Program entry point.
	 *
	 * @param args - program arguments
	 * @throws Throwable
	 * @throws SQLException
	 */
	public static void main(String[] args) throws Exception {
		Path configPath = Path.of("config/");
		String logLevel = null;
		Path tokenFile = null;

		int length = args.length;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg.toLowerCase()) {

			case "-config" -> {
				if (hasArg(i, length, "-config")) {
					i++;
					configPath = Path.of(StringUtils.strip(args[i], "\""));
				}
			}

			case "-loglevel" -> {
				if (hasArg(i, length, "-loglevel")) {
					i++;
					String tmp = args[i];
					if (StringUtils.equalsAnyIgnoreCase(tmp, "info", "debug", "trace")) {
						logLevel = tmp.toUpperCase();
						logger.info("Setting logging level to: " + logLevel);
					}
				}
			}

			case "-tokenfile" -> {
				if (hasArg(i, length, "-tokenfile")) {
					i++;
					tokenFile = Path.of(StringUtils.strip(args[i], "\""));
				}
			}
			}
		}

		// Parse program arguments
		params = new ProgramArguments(args);

		// Load settings
		settings = new WatameBotSettings(configPath, tokenFile);

		ImmutableConfiguration config = settings.getConfiguration();

		// Enable ANSI console
		if (config.getBoolean("ansiConsole", true)) {
			logger.info("Installing ANSI console");
			AnsiConsole.systemInstall();
		}

		// Set our log level
		if (logLevel == null)
			logLevel = config.getString("logLevel", "info");
		System.setProperty("LOG_LEVEL", logLevel);
		restartLogging();

		// Attempt to obtain instance lock
		if (config.getBoolean("singleInstance.enabled", true))
			getLock(config.getInt("singleInstance.retries", 5));

		System.out.println();

		// First call of WatameBot class. Will cause instance creation
		WatameBot watame = WatameBot.INSTANCE;

		watame.start();
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
			try (InputStream in = new ModuleResource("watamebot", "/logback.xml").openStream()) {
				configurator.doConfigure(in);
			}
		} catch (IOException | JoranException e) {

		}

		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

	/**
	 * Get the {@link ProgramArguments} of this application.
	 *
	 * @return flags, arguments and parameters used to launch this application
	 */
	@Nonnull
	@Deprecated
	static ProgramArguments getProgramArguments() { return params; }

	@Nonnull
	static WatameBotSettings getSettings() { return settings; }
}
