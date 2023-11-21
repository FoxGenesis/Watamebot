package net.foxgenesis.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SystemInformation {
	public static synchronized Path dumpSystemInformation(Path path) throws IOException {
		if (path == null)
			path = Path.of("sysinfo.txt");

		if (Files.exists(path))
			return path;

		ProcessBuilder builder = new ProcessBuilder("msinfo32", "/report", path.toAbsolutePath().toString());
		builder.start().onExit().join();

		while (!Files.exists(path))
			Thread.onSpinWait();

		return path;
	}
}
