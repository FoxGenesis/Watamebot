package net.foxgenesis.property;

public interface PropertyMapping {
	String getAsPlainText();
	Object getAsObject();
	String getAsString();
	boolean getAsBoolean();
	int getAsInt();
	float getAsFloat();
	double getAsDouble();
	long getAsLong();
	
	String[] getAsStringArray();
	boolean[] getAsBooleanArray();
	byte[] getAsByteArray();
	int[] getAsIntegerArray();
	float[] getAsFloatArray();
	double[] getAsDoubleArray();
	long[] getAsLongArray();
	
	 boolean isUserReadable();
	 PropertyType getType();
	 long getLookup();
}
