package net.foxgenesis.property;

public class UnmodifiablePropertyException extends UnsupportedOperationException {
	private static final long serialVersionUID = 6687883111943371434L;

	private final ImmutableProperty<?, ?, ?> property;

	public UnmodifiablePropertyException(ImmutableProperty<?, ?, ?> property, String message) {
		super(message);
		this.property = property;
	}

	ImmutableProperty<?, ?, ?> getProperty() { return property; }
}