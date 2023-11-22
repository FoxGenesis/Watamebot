package net.foxgenesis.property2;

public interface PropertyData<T> extends Comparable<T> {
	String getDisplayName();

	String getFieldName();

	String getDescription();

	String getCategory();

	String getKeywords();

	String getSuffix();

	String getPlaceholder();

	T getDefaultValue();

	T getMinValue();

	T getMaxValue();

	String getInputType();

	default PropertyType getType() {
		return PropertyType.valueOf(getInputType());
	}
}
