package net.foxgenesis.watame.plugin.require;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.foxgenesis.util.resource.ConfigType;
import net.foxgenesis.watame.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Annotation used on {@link Plugin} classes to request the loading of custom
 * configuration files.
 *
 * @author Ashley
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginConfiguration {

	/**
	 * The {@code ID} of this configuration.
	 *
	 * @return Returns a string representing the {@code ID} of this configuration
	 */
	@NotNull
	public String identifier();

	/**
	 * The path to the default configuration file inside the jar.
	 *
	 * @return Returns a string pointing to the default configuration file
	 */
	@NotNull
	public String defaultFile();

	/**
	 * The path to store the configuration file <b>outside</b> the jar file. This
	 * path is relative to the plugin configuration directory.
	 *
	 * @return Returns the path to store the configuration outside the jar relative
	 *         to the plugin configuration directory
	 */
	@NotNull
	public String outputFile();

	/**
	 * The type of configuration to parse as.
	 * <p>
	 * <b>Default:</b> {@link ConfigType#PROPERTIES}
	 * </p>
	 *
	 * @return Returns the {@link ConfigType} of this configuration
	 */
	@NotNull
	public ConfigType type() default ConfigType.PROPERTIES;
}
