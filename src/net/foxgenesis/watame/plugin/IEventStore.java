package net.foxgenesis.watame.plugin;

public interface IEventStore {
	void registerListener(Plugin plugin, Object listener);
}
