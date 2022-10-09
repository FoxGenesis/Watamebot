package net.foxgenesis.watame.functionality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;

/**
 * Class used to contain plugin interactions
 * 
 * @author Ashley
 *
 */
public class InteractionHandler implements IInteractionHandler {

	/**
	 * Operator to reduce collections to a single instance
	 */
	private static final BinaryOperator<Collection<CommandData>> commandDataReduction = (a, b) -> {
		a.addAll(b);
		return a;
	};

	/**
	 * Collection of functions to generate command data
	 */
	private Set<Function<Guild, Collection<CommandData>>> guildInteractions = new HashSet<>();

	/**
	 * Collection of global command data
	 */
	private Set<CommandData> globalInteractions = new HashSet<>();

	/**
	 * Generate a {@link Collection} of {@link CommandData} containing all guild and
	 * global interactions.
	 * 
	 * @param guildCache - {@link JDA} guild cache
	 * @return a {@link Collection<CommandData>} containing interactions
	 */
	public Collection<CommandData> getAllInteractions(SnowflakeCacheView<Guild> guildCache) {
		Set<CommandData> interactions = new HashSet<>(globalInteractions);

		interactions.addAll(guildCache.applyStream(guilds -> guilds.map(guild -> getInteractionsForGuild(guild))
				.filter(cmdData -> cmdData != null).reduce(commandDataReduction).orElse(new ArrayList<CommandData>())));

		return interactions;
	}

	/**
	 * Helper function to map all interaction functions into a single collection.
	 * @param guild - {@link Guild} to get {@link CommandData} for
	 * @return a {@link Collection<CommandData>} containing interactions for {@code guild}
	 */
	private Collection<CommandData> getInteractionsForGuild(Guild guild) {
		return guildInteractions.parallelStream().map(func -> func.apply(guild)).filter(cmd -> cmd != null)
				.reduce(commandDataReduction).orElse(new ArrayList<CommandData>());
	}

	@Override
	public boolean registerGlobalInteraction(CommandData data) {
		return globalInteractions.add(data);
	}

	@Override
	public boolean registerGuildInteractions(Function<Guild, Collection<CommandData>> dataList) {
		return guildInteractions.add(dataList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(globalInteractions, guildInteractions);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InteractionHandler other = (InteractionHandler) obj;
		return Objects.equals(globalInteractions, other.globalInteractions)
				&& Objects.equals(guildInteractions, other.guildInteractions);
	}

	@Override
	public String toString() {
		return "InteractionHandler [guildInteractions=" + guildInteractions + ", globalInteractions="
				+ globalInteractions + "]";
	}
}
