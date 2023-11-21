package net.foxgenesis.http;

import org.jetbrains.annotations.NotNull;

public record ApiKey(@NotNull KeyType type, @NotNull String name, @NotNull String token) {

	public static enum KeyType {
		HEADER, PARAMETER
	}
}