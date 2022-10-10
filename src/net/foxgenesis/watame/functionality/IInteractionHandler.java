package net.foxgenesis.watame.functionality;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Interface used to communicate with {@link InteractionHandler}.
 * 
 * @author Ashley
 *
 */
public interface IInteractionHandler {
	/**
	 * Register multiple global interactions
	 * 
	 * @param dataList - interactions to add
	 * @see #registerGlobalInteraction(CommandData)
	 */
	public default void registerGlobalInteractions(CommandData... dataList) {
		for (CommandData data : dataList)
			registerGlobalInteraction(data);
	}

	/**
	 * Register a single global interaction
	 * 
	 * @param data - interaction to add
	 * @return if the interaction was added
	 * @see #registerGlobalInteractions(CommandData...)
	 */
	public boolean registerGlobalInteraction(CommandData data);

	/**
	 * Register multiple guild interactions
	 * 
	 * @param dataList - a function mapping a guild to a collection of interactions
	 * @return if the function was added
	 * @see #registerGuildInteraction(Function)
	 */
	public boolean registerGuildInteractions(Function<Guild, Collection<CommandData>> dataList);

	/**
	 * Register a single guild interaction.
	 * <p>
	 * This call is effectively equivalent to: <blockquote>
	 * 
	 * <pre>
	 * registerGuildInteractions(guild -> List.of(data.apply(guild)))
	 * </pre>
	 * 
	 * </blockquote>
	 * </p>
	 * 
	 * @param data - a function mapping a guild to an interaction
	 * @return if the function was added
	 * @see #registerGuildInteractions(Function)
	 */
	public default boolean registerGuildInteraction(Function<Guild, CommandData> data) {
		return registerGuildInteractions(guild -> List.of(data.apply(guild)));
	}
}
