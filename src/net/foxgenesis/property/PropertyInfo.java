package net.foxgenesis.property;

public record PropertyInfo(int id, String category, String name, boolean modifiable, PropertyType type) {

	private static final String DISPLAY_FORMAT = "%d (%s) | [%s] %s";

	public String getDisplayString() {
		return DISPLAY_FORMAT.formatted(id, type, category, name);
	}
	
	@Override
	public String toString() {
		return getDisplayString();
	}
}
