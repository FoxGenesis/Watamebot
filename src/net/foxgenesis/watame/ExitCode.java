package net.foxgenesis.watame;

import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

/**
 * Enum of program exit codes
 *
 * @author Ashley
 */
public enum ExitCode {
	INSTANCE_ALREADY_RUNNING(1), SETUP_ERROR(2), RESOURCE_IO_ERROR(5),

	NO_TOKEN(12), INVALID_TOKEN(13),

	JDA_BUILD_FAIL(20),

	DATABASE_NOT_CONNECTED(30), DATABASE_INVALID_SETUP_FILE(31), DATABASE_SETUP_ERROR(32), DATABASE_ACCESS_ERROR(33),
	DATABASE_STATEMENT_MISSING(34), DATABASE_STATEMENT_ERROR(35);

	/**
	 * exit code number
	 */
	private final int statusCode;

	/**
	 * Create a new {@link ExitCode} with a specified number.
	 *
	 * @param statusCode - exit code number
	 */
	ExitCode(int statusCode) { this.statusCode = statusCode; }

	/**
	 * Returns the {@link ExitCode}'s number.
	 *
	 * @return exit code
	 */
	public Integer getCode() { return statusCode; }

	/**
	 * Exit the program with a specific {@code message} and {@link Throwable}.
	 * <p>
	 * The call {@code exitCode.exitProgram(null, null)} is effectively equivalent
	 * to the call:
	 * </p>
	 * <blockquote>
	 *
	 * <pre>
	 * exitCode.exitProgram()
	 * </pre>
	 *
	 * </blockquote>
	 *
	 *
	 * @see #programExit()
	 * @param exitMessage - Exit message to log
	 * @param thrown      - Throwable to log
	 * @throws Exception
	 */
	public void programExit(String exitMessage, Exception thrown) throws Exception {
		// if(exitMessage != null || thrown != null)
		LoggerFactory.getLogger(Logger.GLOBAL_LOGGER_NAME).error(exitMessage == null ? name() : exitMessage,
				thrown);
		programExit();
		if(thrown != null)
			throw thrown;
	}

	/**
	 * Exit the program with a specific {@link Throwable}.
	 * <p>
	 * The call {@code exitCode.exitProgram(thrown)} is effectively equivalent to
	 * the call:
	 * </p>
	 * <blockquote>
	 *
	 * <pre>
	 * exitCode.exitProgram(null, thrown)
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @see #programExit(String, Exception)
	 * @param thrown - Throwable to log
	 * @throws Exception
	 */
	public void programExit(Exception thrown) throws Exception { programExit(null, thrown); }

	/**
	 * Exit the program with a specific {@code message}.
	 * <p>
	 * The call {@code exitCode.exitProgram(message)} is effectively equivalent to
	 * the call:
	 * </p>
	 * <blockquote>
	 *
	 * <pre>
	 * exitCode.exitProgram(message, null)
	 * </pre>
	 *
	 * </blockquote>
	 *
	 *
	 * @see #programExit(String, Exception)
	 * @param exitMessage - Exit message to log
	 * @throws Exception
	 */
	public void programExit(String exitMessage) throws Exception { programExit(exitMessage, null); }

	/**
	 * Exit the program with this {@link ExitCode}'s {@code "exit code"}.
	 * <p>
	 * This method is effectively equivalent to the call:
	 * </p>
	 * <blockquote>
	 *
	 * <pre>
	 * System.exit(n)
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @throws Exception
	 * @see #programExit(String, Exception)
	 */
	public void programExit() throws Exception { System.exit(getCode()); }
}
