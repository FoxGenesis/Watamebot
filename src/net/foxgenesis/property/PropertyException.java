package net.foxgenesis.property;

public class PropertyException extends RuntimeException {

	private static final long serialVersionUID = 6766944452136852281L;
	
	public PropertyException(Throwable t) {
		super(t);
	}

	public PropertyException(String msg) {
		super(msg);
	}
	
	public PropertyException(String msg, Throwable t) {
		super(msg, t);
	}
}
