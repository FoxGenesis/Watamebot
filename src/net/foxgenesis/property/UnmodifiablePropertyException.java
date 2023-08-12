package net.foxgenesis.property;

public class UnmodifiablePropertyException extends PropertyException {

	private static final long serialVersionUID = 4796449383013631106L;

	public UnmodifiablePropertyException(String msg, Throwable t) {
		super(msg, t);
	}

	public UnmodifiablePropertyException(String msg) {
		super(msg);
	}
	
	public UnmodifiablePropertyException(Throwable t) {
		super(t);
	}
}
