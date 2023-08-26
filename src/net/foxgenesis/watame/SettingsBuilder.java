package net.foxgenesis.watame;

import java.nio.file.Path;

import net.foxgenesis.watame.Settings.LogLevel;

import org.jetbrains.annotations.Nullable;

public class SettingsBuilder {

	Path configPath = Path.of("config");

	@Nullable
	LogLevel logLevel = null;

	@Nullable
	String tokenFile = null;

	@Nullable
	String pbToken = null;

	public void setConfigPath(Path path) {
		this.configPath = path;
	}

	public void setTokenFile(String tokenFile) {
		this.tokenFile = tokenFile;
	}

	public void setLogLevel(LogLevel level) {
		this.logLevel = level;
	}

	public void setPushbulletToken(String token) {
		this.pbToken = token;
	}
}
