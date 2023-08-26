package net.foxgenesis.watame.plugin;

import java.lang.module.ModuleDescriptor.Version;
import java.nio.file.Path;

/**
 * Record class containing all information about a {@link Plugin}.
 *
 * @param getID                - plugin identifier
 * @param getDisplayName       - name used for display
 * @param getVersion           - plugin {@link Version}
 * @param getDescription       - description of the plugin
 * @param providesCommands     - if the plugin provides commands
 * @param requiresDatabase     - if the plugin requires access to the database
 * @param getConfigurationPath - the {@link Path} to the plugin's configuration
 *                             folder
 *
 * @author Ashley
 */
public record PluginInformation(String getID, String getDisplayName, Version getVersion, String getDescription,
		boolean providesCommands, boolean requiresDatabase, Path getConfigurationPath) {

	/**
	 * Get a string displaying the plugin's {@code displayName} and {@code version}.
	 *
	 * @return Returns a string representing the plugin's name and version
	 */
	public String getDisplayInfo() {
		return getDisplayName + " v" + getVersion;
	}
}
