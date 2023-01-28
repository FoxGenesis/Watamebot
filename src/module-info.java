/**
 * Watamebot core module
 * 
 * @author Ashley
 *
 * @provides net.foxgenesis.watame.plugin.IPlugin
 * @uses net.foxgenesis.watame.plugin.IPlugin
 */
module watamebot {
	requires transitive net.dv8tion.jda;
	requires transitive org.json;
	requires transitive org.slf4j;
	requires transitive java.sql;
	requires java.logging;
	requires jsr305;
	requires logback.classic;
	requires logback.core;
	requires org.fusesource.jansi;
	requires org.apache.commons.lang3;

	exports net.foxgenesis.config;
	exports net.foxgenesis.config.fields;
	exports net.foxgenesis.property;
	exports net.foxgenesis.log;
	exports net.foxgenesis.watame.sql;
	exports net.foxgenesis.watame;
	exports net.foxgenesis.watame.plugin;
	exports net.foxgenesis.util;
	exports net.foxgenesis.util.function;
	exports net.foxgenesis.watame.util;
	exports net.foxgenesis.watame.property;

	uses net.foxgenesis.watame.plugin.IPlugin;
	opens resources;
}