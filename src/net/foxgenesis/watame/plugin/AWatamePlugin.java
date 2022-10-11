package net.foxgenesis.watame.plugin;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.watame.WatameBot;

/**
 * Abstract class containing the base code for modular bot functionality. This
 * class also extends {@link ListenerAdapter} for ease of use.
 * 
 * @author Ashley
 *
 */
public abstract class AWatamePlugin extends ListenerAdapter implements IStartup {

	private PluginProperties properties;

	/**
	 * The instance of {@link WatameBot} this functionality is added to.
	 */
	private WatameBot watame;

	/**
	 * Object used to contain all plugin interactions
	 */
	private InteractionHandler interactions = new InteractionHandler();
	
	protected AWatamePlugin() {}


	@Override
	public void preInit(@Nonnull WatameBot watame) {
		// Set our current instance of the bot
		this.watame = watame;
	}
	
	void setProperties(@Nonnull PluginProperties properties) {
		Objects.nonNull(properties);
		this.properties = properties;
	}
	
	void setWatame(@Nonnull WatameBot watame) {
		this.watame = watame;
	}

	/**
	 * Get the instance of {@link WatameBot} this functionality is bound to.
	 * 
	 * @return Instance of {@link WatameBot}
	 */
	protected final WatameBot getWatame() {
		return watame;
	}

	/**
	 * Get name of this functionality. This serves as an identifier.
	 * 
	 * @return The name of this functionality
	 */
	public String getName() {
		return properties.getProperty("name", "My WatameBot Plugin");
	}

	/**
	 * Get the {@link InteractionHandler} interface used to register interactions
	 * with {@link WatameBot}.
	 * 
	 * @return this plugins {@link InteractionHandler} interface
	 */
	public IInteractionHandler getInteractionHandler() {
		return interactions;
	}
}
