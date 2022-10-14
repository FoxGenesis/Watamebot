package net.foxgenesis.watame.plugin;

/**
 * Abstract class containing the base code for modular bot functionality.
 * 
 * @author Ashley
 *
 */
public abstract class AWatamePlugin implements IPlugin {

	private PluginProperties properties;

	protected AWatamePlugin() {
	}

	@Override
	public PluginProperties getProperties() {
		return properties;
	}
}
