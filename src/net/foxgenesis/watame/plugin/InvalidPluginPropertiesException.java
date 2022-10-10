package net.foxgenesis.watame.plugin;

public class InvalidPluginPropertiesException extends UnsupportedOperationException {
	private static final long serialVersionUID = 384299482971559711L;

	public InvalidPluginPropertiesException() {
	}

	public InvalidPluginPropertiesException(String message) {
		super(message);
	}
}
