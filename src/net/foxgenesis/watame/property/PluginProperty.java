package net.foxgenesis.watame.property;

import java.io.InputStream;
import java.io.Serializable;

import net.dv8tion.jda.api.entities.Guild;

public interface PluginProperty extends ImmutablePluginProperty {

	boolean set(Guild lookup, Serializable obj);

	boolean set(Guild lookup, byte[] data);

	boolean set(Guild lookup, InputStream in);

	/**
	 * Remove this property from the configuration
	 * 
	 * @param lookup - property lookup
	 * 
	 * @return Returns {@code true} if the property with the specified
	 *         {@code lookup} was removed from the configuration
	 */
	boolean remove(Guild lookup);
}
