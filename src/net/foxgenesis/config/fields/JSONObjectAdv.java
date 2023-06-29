package net.foxgenesis.config.fields;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import net.foxgenesis.util.function.TriConsumer;
@Deprecated(forRemoval = true)
public class JSONObjectAdv extends JSONObject {

	private final TriConsumer<String, Object, Boolean> update;

	public JSONObjectAdv(@NotNull String json, @NotNull TriConsumer<String, Object, Boolean> update) {
		super(json);
		this.update = Objects.requireNonNull(update);
	}

	@Override
	public JSONObjectAdv put(String key, Object value) {
		super.put(key, value);
		if (this.update != null)
			this.update.accept(key, value, false);
		return this;
	}

	public JSONObjectAdv putIfAbsent(@NotNull String key, Object value) {
		if (!has(key))
			put(key, value);
		return this;
	}

	@Override
	public JSONObjectAdv remove(String key) {
		super.remove(key);
		if (this.update != null)
			this.update.accept(key, null, true);
		return this;
	}
}
