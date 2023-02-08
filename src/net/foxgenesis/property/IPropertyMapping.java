package net.foxgenesis.property;

import javax.annotation.Nonnull;

/**
 * Interface used to map a property's raw data into a usable one.
 * 
 * @author Ashley
 * @see IPropertyProvider
 * @see IPropertyField
 */
public interface IPropertyMapping {

	/**
	 * Return this property as a string.
	 * 
	 * @return The property's string value
	 */
	@Nonnull
	public String getAsString();

	/**
	 * Return this property as a long.
	 * 
	 * @return The property's long value
	 */
	public long getAsLong();

	/**
	 * Return this property as a double.
	 * 
	 * @return The property's double value
	 */
	public double getAsDouble();

	/**
	 * Return this property as a float.
	 * 
	 * @return The property's float value
	 */
	public float getAsFloat();

	/**
	 * Return this property as an integer.
	 * 
	 * @return The property's integer value
	 */
	public int getAsInt();

	/**
	 * Return this property as a boolean.
	 * 
	 * @return The property's boolean value
	 */
	public boolean getAsBoolean();
}
