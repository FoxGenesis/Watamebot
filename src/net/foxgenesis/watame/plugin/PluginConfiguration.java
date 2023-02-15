package net.foxgenesis.watame.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginConfiguration {

	public String identifier();
	
	public String defaultFile();

	public String outputFile();
}
