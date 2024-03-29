package net.foxgenesis.watame.plugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class EventStore implements IEventStore {
	private static final Logger logger = LoggerFactory.getLogger(EventStore.class);

	private final ConcurrentHashMap<Plugin, Set<Object>> store = new ConcurrentHashMap<>();
	private final JDABuilder builder;

	private JDA jda;

	public EventStore(JDABuilder builder) {
		this.builder = Objects.requireNonNull(builder);
	}

	public void register(Plugin plugin) {
		store.putIfAbsent(plugin, Collections.synchronizedSet(new HashSet<>()));
	}

	public void unregister(Plugin plugin) {
		Objects.requireNonNull(plugin);

		Set<Object> objs = store.remove(plugin);

		if (!(objs == null || objs.isEmpty())) {
			Object[] l = objs.toArray();
			logger.debug("Removing {} listeners from {}", l.length, plugin.getInfo().getDisplayName());
			builder.removeEventListeners(l);

			if (jda != null)
				jda.removeEventListener(l);

			objs.clear();
		}
	}

	@Override
	public void registerListeners(Plugin plugin, Object... listener) {
		Objects.requireNonNull(plugin);
		Objects.requireNonNull(listener);

		if (!store.containsKey(plugin))
			throw new IllegalArgumentException("Provided plugin is not registered!");
		Set<Object> listeners = store.get(plugin);

		logger.debug("Adding {} listeners from {}", listener.length, plugin.getInfo().getDisplayName());
		Collections.addAll(listeners, listener);
		
		builder.addEventListeners(listener);
		if (jda != null) {
			logger.debug("Adding {} listeners from {} to JDA", listener.length, plugin.getInfo().getDisplayName());
			jda.addEventListener(listener);
		}
	}

	@Override
	public void unregisterListeners(Plugin plugin, Object... listener) {
		if (!store.containsKey(plugin))
			throw new IllegalArgumentException("Provided plugin is not registered!");
		Set<Object> listeners = store.get(plugin);
		
		logger.debug("Removing {} listeners from {}", listener.length, plugin.getInfo().getDisplayName());
		for (Object l : listener)
			listeners.remove(l);
		
		builder.removeEventListeners(listener);
		if (jda != null) {
			logger.debug("Removing {} listeners from {} in JDA", listener.length, plugin.getInfo().getDisplayName());
			jda.removeEventListener(listener);
		}
	}

	public synchronized void setJDA(JDA jda) {
		this.jda = jda;
	}
}
