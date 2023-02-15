package net.foxgenesis.watame.sql;

import java.util.function.Supplier;

import javax.sql.DataSource;

import net.foxgenesis.util.ResourceUtils.ModuleResource;
@Deprecated(forRemoval = true)
public record DatabaseProperties(DataSource source, ModuleResource setupFile, ModuleResource operationsFile,
		ModuleResource callableOperationsFile, String name) {
	public DatabaseProperties(Supplier<DataSource> source, ModuleResource setupFile, ModuleResource operationsFile,
			ModuleResource callableOperationsFile, String name) {
		this(source.get(), setupFile, operationsFile, callableOperationsFile, name);
	}

	public DatabaseProperties(Supplier<DataSource> source, ModuleResource operationsFile,
			ModuleResource callableOperationsFile, String name) {
		this(source.get(), null, operationsFile, callableOperationsFile, name);
	}

	public DatabaseProperties(DataSource source, ModuleResource operationsFile, ModuleResource callableOperationsFile,
			String name) {
		this(source, null, operationsFile, callableOperationsFile, name);
	}

	public DatabaseProperties(Supplier<DataSource> source, ModuleResource operationsFile,
			ModuleResource callableOperationsFile) {
		this(source.get(), null, operationsFile, callableOperationsFile, null);
	}

	public DatabaseProperties(DataSource source, ModuleResource operationsFile, ModuleResource callableOperationsFile) {
		this(source, null, operationsFile, callableOperationsFile, null);
	}

	public DatabaseProperties(Supplier<DataSource> source) { this(source.get(), null, null, null); }

	public DatabaseProperties(DataSource source) { this(source, null, null, null); }
}
