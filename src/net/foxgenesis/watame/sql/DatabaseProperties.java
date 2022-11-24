package net.foxgenesis.watame.sql;

import java.net.URL;
import java.util.function.Supplier;

import javax.sql.DataSource;

public record DatabaseProperties(DataSource source, URL setupFile, URL operationsFile, String name) {
	public DatabaseProperties(Supplier<DataSource> source, URL setupFile, URL operationsFile, String name) {
		this(source.get(), setupFile, operationsFile, name);
	}

	public DatabaseProperties(Supplier<DataSource> source, URL operationsFile, String name) {
		this(source.get(), null, operationsFile, name);
	}

	public DatabaseProperties(DataSource source, URL operationsFile, String name) {
		this(source, null, operationsFile, name);
	}

	public DatabaseProperties(Supplier<DataSource> source, URL operationsFile) {
		this(source.get(), null, operationsFile, null);
	}

	public DatabaseProperties(DataSource source, URL operationsFile) { this(source, null, operationsFile, null); }

	public DatabaseProperties(Supplier<DataSource> source) { this(source.get(), null, null, null); }

	public DatabaseProperties(DataSource source) { this(source, null, null, null); }
}
