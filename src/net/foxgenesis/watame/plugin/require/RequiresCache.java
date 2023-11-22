package net.foxgenesis.watame.plugin.require;

import java.util.EnumSet;

import net.dv8tion.jda.api.utils.cache.CacheFlag;

public interface RequiresCache {

	EnumSet<CacheFlag> getRequiredCaches();
}
