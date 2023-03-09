package net.foxgenesis.util.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class FormattedModuleResource extends ModuleResource {
	private static final Pattern pattern = Pattern.compile("\\{\\{(.*?)}}");

	private final Map<String, String> mappings;

	public FormattedModuleResource(@Nonnull String module, @Nonnull String resource,
			@Nonnull Map<String, String> mappings) {
		super(module, resource);
		this.mappings = Objects.requireNonNull(mappings);
	}

	@Override
	public InputStream openStream() throws IOException {
		if (mappings.isEmpty())
			return super.openStream();
		try (InputStream in = super.openStream()) {
			return new ByteArrayInputStream(format(new String(in.readAllBytes())).getBytes());
		}

	}

	private String format(String str) {
		return pattern.matcher(str).replaceAll(result -> mappings.getOrDefault(result.group(1), "null"));
	}
}
