package net.foxgenesis.watame;

import java.io.File;

import javax.annotation.Nonnull;

import net.foxgenesis.util.resource.ModuleResource;

public final class Constants {

	public static final ModuleResource LOGGING_SETTINGS = new ModuleResource("watamebot", "/logback.xml");
	
	public static final ModuleResource DATABASE_SETUP_FILE = resource("table setup.sql");
	public static final ModuleResource DATABASE_OPERATIONS_FILE = resource("sql statements.kvp");
	public static final ModuleResource DATABASE_SETTINGS_FILE = resource("defaults/database.properties");

	public static final File PLUGINS_FOLDER = new File("plugins");

	private static final ModuleResource resource(@Nonnull String path) {
		return new ModuleResource("watamebot", "/META-INF/" + path);
	}

	public static final class Colors {
		public static final int ERROR = 0xF44336;
		public static final int WARNING = 0xFFEB3B;
		public static final int WARNING_DARK = 0xFFC107;
		public static final int INFO = 0x2196F3;
		public static final int SUCCESS = 0x4CAF50;
	}
}
