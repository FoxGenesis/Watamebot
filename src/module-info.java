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
	requires transitive com.zaxxer.hikari;
	requires transitive org.apache.commons.configuration2;

	requires org.fusesource.jansi;
	requires org.apache.commons.lang3;
	requires ch.qos.logback.core;
	requires ch.qos.logback.classic;
	requires jsr305;
	requires java.desktop;

	exports net.foxgenesis.config;
	exports net.foxgenesis.config.fields;
	exports net.foxgenesis.database;
	exports net.foxgenesis.executor;
	exports net.foxgenesis.property;
	exports net.foxgenesis.log;
	exports net.foxgenesis.watame.sql;
	exports net.foxgenesis.watame;
	exports net.foxgenesis.watame.plugin;
	exports net.foxgenesis.util;
	exports net.foxgenesis.util.resource;
	exports net.foxgenesis.util.function;
	exports net.foxgenesis.watame.property;

	uses net.foxgenesis.watame.plugin.Plugin;

	provides net.foxgenesis.watame.plugin.Plugin with net.foxgenesis.watame.command.IntegratedCommands;
}