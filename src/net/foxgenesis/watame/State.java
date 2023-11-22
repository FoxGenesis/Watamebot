package net.foxgenesis.watame;

/**
 * States {@link WatameBot} goes through on startup.
 *
 * @author Ashley
 */
public enum State {
	/**
	 * NEED_JAVADOC
	 */
	CONSTRUCTING,
	/**
	 * NEED_JAVADOC
	 */
	PRE_INIT,
	/**
	 * NEED_JAVADOC
	 */
	INIT,
	/**
	 * NEED_JAVADOC
	 */
	POST_INIT,
	/**
	 * WatameBot has finished all loading stages and is running
	 */
	RUNNING,
	/**
	 * WatameBot is shutting down
	 */
	SHUTDOWN;
}