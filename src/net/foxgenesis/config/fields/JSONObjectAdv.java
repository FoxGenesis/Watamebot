package net.foxgenesis.config.fields;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.json.JSONObject;

import net.foxgenesis.util.function.TriConsumer;

public class JSONObjectAdv extends JSONObject {

	@Nonnull
	private final TriConsumer<String, Object, Boolean> update;

	public JSONObjectAdv(@Nonnull String json, @Nonnull TriConsumer<String, Object, Boolean> update) {
		super(json);
		Objects.nonNull(update);
		this.update = update;
	}

	@Override
	public JSONObjectAdv put(@Nonnull String key, Object value) {
		super.put(key, value);
		if (this.update != null)
			this.update.accept(key, value, false);
		return this;
	}

	public JSONObjectAdv putIfAbsent(@Nonnull String key, Object value) {
		if (!has(key))
			put(key, value);
		return this;
	}

	@Override
	public JSONObjectAdv remove(@Nonnull String key) {
		super.remove(key);
		if (this.update != null)
			this.update.accept(key, null, true);
		return this;
	}
}
