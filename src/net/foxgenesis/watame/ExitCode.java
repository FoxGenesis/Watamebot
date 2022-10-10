package net.foxgenesis.watame;

import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

/**
 * Enum of program exit codes
 * @author Ashley
 */
public enum ExitCode {
	INSTANCE_ALREADY_RUNNING(1),
	RESOURCE_IO_ERROR(5),
	
	NO_TOKEN(12),
	INVALID_TOKEN(13),
	
	JDA_BUILD_FAIL(20),
	
	DATABASE_NOT_CONNECTED(30),
	DATABASE_INVALID_SETUP_FILE(31),
	DATABASE_SETUP_ERROR(32),
	DATABASE_ACCESS_ERROR(33),
	DATABASE_STATEMENT_MISSING(34),
	DATABASE_STATEMENT_ERROR(35);
	
	
	/**
	 * exit code number
	 */
	private final int statusCode;
	
	/**
	 * Create a new {@link ExitCode} with a specified number.
	 * @param statusCode - exit code number
	 */
	ExitCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Returns the {@link ExitCode}'s number.
	 * @return exit code
	 */
	public Integer getCode() {
		return statusCode;
	}
	
	/**
	 * Exit the program with a specific {@code message} and {@link Throwable}.
	 * <p>
	 * The call {@code exitCode.exitProgram(null, null)} is effectively equivalent to
     * the call:
     * <blockquote><pre>
     * exitCode.exitProgram()
     * </pre></blockquote>
	 * </p>
	 * @see #programExit()
	 * @param exitMessage - Exit message to log
	 * @param thrown - Throwable to log
	 */
	public void programExit(String exitMessage, Throwable thrown) {
		//if(exitMessage != null || thrown != null)
		LoggerFactory.getLogger(Logger.GLOBAL_LOGGER_NAME).error(exitMessage == null? this.name() : exitMessage, thrown);
		programExit();
	}
	
	/**
	 * Exit the program with a specific {@link Throwable}.
	 * <p>
	 * The call {@code exitCode.exitProgram(thrown)} is effectively equivalent to
     * the call:
     * <blockquote><pre>
     * exitCode.exitProgram(null, thrown)
     * </pre></blockquote>
	 * </p>
	 * @see #programExit(String, Throwable)
	 * @param thrown - Throwable to log
	 */
	public void programExit(Throwable thrown) {
		programExit(null, thrown);
	}
	
	/**
	 * Exit the program with a specific {@code message}.
	 * <p>
	 * The call {@code exitCode.exitProgram(message)} is effectively equivalent to
     * the call:
     * <blockquote><pre>
     * exitCode.exitProgram(message, null)
     * </pre></blockquote>
	 * </p>
	 * @see #programExit(String, Throwable)
	 * @param exitMessage - Exit message to log
	 */
	public void programExit(String exitMessage) {
		programExit(exitMessage, null);
	}
	
	/**
	 * Exit the program with this {@link ExitCode}'s &quot{@code exit code}&quot.
	 * <p>
	 * This method is effectively equivalent to
     * the call:
     * <blockquote><pre>
     * System.exit(n)
     * </pre></blockquote>
	 * </p>
	 * 
	 * @see #programExit(String, Throwable)
	 */
	public void programExit() {
		System.exit(getCode());
	}
}
