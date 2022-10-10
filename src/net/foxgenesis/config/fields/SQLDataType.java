package net.foxgenesis.config.fields;

public enum SQLDataType {
	CHAR, VARCHAR, SMALLINT, INT, DEC, NUMERIC, REAL, FLOAT, DOUBLE_PRECISION, DATE, TIME, TIMESTAMP, CLOB, BLOB, JSON;

	public final String name = this.name();

	public String ofLength(int l) {
		return toString() + '(' + l + ')';
	}

	public String ofPrecision(int p) {
		return ofLength(p);
	}

	public String ofPrecisionAndScale(int p, int s) {
		return toString() + '(' + p + ',' + s + ')';
	}

	@Override
	public String toString() {
		return this.name();
	}
}
