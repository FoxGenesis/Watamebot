package net.foxgenesis.property3;

public interface PropertyMapping {

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
}
