package net.foxgenesis.property;

public class UnmodifiablePropertyException extends UnsupportedOperationException {
	private static final long serialVersionUID = 6687883111943371434L;

	private final IPropertyField<?, ?,?> property;

	public UnmodifiablePropertyException(IPropertyField<?, ?, ?> property, String message) {
		super(message);
		this.property = property;
	}

	IPropertyField<?, ?, ?> getProperty() { return property; }
}