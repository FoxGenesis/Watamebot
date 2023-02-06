package net.foxgenesis.watame.plugin;

public class SeverePluginException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1101112080896880561L;

	/**
	 * If this exception is fatal
	 */
	private final boolean fatal;

	public SeverePluginException(String message, Throwable thrown) { this(message, thrown, true); }

	public SeverePluginException(String message) { this(message, true); }

	public SeverePluginException(Throwable thrown) { this(thrown, true); }

	public SeverePluginException(String message, boolean fatal) {
		super(message);
		this.fatal = fatal;
	}

	public SeverePluginException(Throwable thrown, boolean fatal) {
		super(thrown);
		this.fatal = fatal;
	}

	public SeverePluginException(String message, Throwable thrown, boolean fatal) {
		super(message, thrown);
		this.fatal = fatal;
	}

	/**
	 * Check if this exception was fatal.
	 * 
	 * @return Returns {@code true} if this exception results in a fatal error
	 */
	public boolean isFatal() { return fatal; }
}
