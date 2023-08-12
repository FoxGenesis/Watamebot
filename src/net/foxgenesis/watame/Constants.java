package net.foxgenesis.watame;

import java.io.File;

import net.foxgenesis.util.resource.ModuleResource;

import org.jetbrains.annotations.NotNull;

public final class Constants {

	public static final ModuleResource LOGGING_SETTINGS = new ModuleResource("watamebot", "/logback.xml");

	public static final ModuleResource DATABASE_SETUP_FILE = resource("table setup.sql");
	public static final ModuleResource DATABASE_OPERATIONS_FILE = resource("sql statements.kvp");
	public static final ModuleResource DATABASE_SETTINGS_FILE = resource("defaults/database.properties");

	public static final File PLUGINS_FOLDER = new File("plugins");

	public static final long PLUGIN_PROPERTY_CACHE_TIME = 5_000;
	
	public static final String DATABASE_TABLE_PROPERTY_INFO = "PropertyInfo";
	public static final String DATABASE_TABLE_PROPERTIES = "Properties";

	private static ModuleResource resource(@NotNull String path) {
		return new ModuleResource("watamebot", "/META-INF/" + path);
	}
}
