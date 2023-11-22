package net.foxgenesis.watame.plugin;

public interface IEventStore {
	void registerListeners(Plugin plugin, Object... listener);

	void unregisterListeners(Plugin plugin, Object... listener);
}
