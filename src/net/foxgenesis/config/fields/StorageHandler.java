package net.foxgenesis.config.fields;

import java.util.function.Consumer;
@Deprecated(forRemoval = true)
public class StorageHandler {
	public Object data;
	private final Consumer<Object> update;

	public StorageHandler(Object data, Consumer<Object> update) {
		this.data = data;
		this.update = update;
	}

	public void update() { this.update.accept(this.data); }
}
