package net.foxgenesis.watame.plugin;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class EventStore implements IEventStore {

	private final ConcurrentHashMap<Plugin, Set<Object>> store = new ConcurrentHashMap<>();
	private final JDABuilder builder;

	private JDA jda;

	public EventStore(JDABuilder builder) { this.builder = Objects.requireNonNull(builder); }

	public void register(Plugin plugin) { store.putIfAbsent(plugin, new HashSet<>()); }

	public synchronized void unregister(Plugin plugin) {
		Objects.requireNonNull(plugin);

		Set<Object> objs = store.remove(plugin);
		
		if (!(objs == null || objs.isEmpty())) {
			builder.removeEventListeners(objs.toArray());
			
			if(jda != null)
				jda.removeEventListener(objs.toArray());
			
			objs.clear();
		}
	}

	@Override
	public synchronized void registerListener(Plugin plugin, Object listener) {
		Objects.requireNonNull(plugin);
		Objects.requireNonNull(listener);

		if (store.containsKey(plugin)) {
			store.get(plugin).add(listener);
			
			builder.removeEventListeners(listener);
			
			if(jda != null)
				jda.addEventListener(listener);
		} else
			throw new IllegalArgumentException("Provided plugin is not registered!");
	}
	
	public synchronized void setJDA(JDA jda) {
		this.jda = jda;
	}
}
