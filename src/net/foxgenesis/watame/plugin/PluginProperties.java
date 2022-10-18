package net.foxgenesis.watame.plugin;

public @interface PluginProperties {
	/**
	 * Get the of the plugin
	 * 
	 * @return The name of this plugin
	 */
	public String name();

	/**
	 * Get the description of the plugin
	 * 
	 * @return The description of this plugin
	 */
	public String description();

	/**
	 * Get the version of this plugin. This is parsed by
	 * {@link Runtime.Version#parse(String)}.
	 * 
	 * @return The version of this plugin.
	 */
	public String version();
}
