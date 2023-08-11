/**
 * Watamebot core module
 * 
 * @author Ashley
 *
 * @provides net.foxgenesis.watame.plugin.Plugin
 * 
 * @uses net.foxgenesis.watame.plugin.Plugin
 */
module watamebot {
	requires transitive net.dv8tion.jda;
	requires transitive org.slf4j;
	requires transitive org.apache.commons.configuration2;

	requires static org.fusesource.jansi;
	requires static org.jetbrains.annotations;

	requires com.zaxxer.hikari;
	requires org.apache.commons.lang3;
	requires ch.qos.logback.core;
	requires ch.qos.logback.classic;
	requires java.sql.rowset;
	requires java.sql;

	exports net.foxgenesis.config;
	exports net.foxgenesis.database;
	exports net.foxgenesis.executor;
	exports net.foxgenesis.property;
	exports net.foxgenesis.property.lck;
	exports net.foxgenesis.log;
	exports net.foxgenesis.watame;
	exports net.foxgenesis.watame.plugin;
	exports net.foxgenesis.util;
	exports net.foxgenesis.util.resource;
	exports net.foxgenesis.util.function;
	exports net.foxgenesis.watame.property;
	exports net.foxgenesis.watame.util;

	uses net.foxgenesis.watame.plugin.Plugin;

	provides net.foxgenesis.watame.plugin.Plugin with net.foxgenesis.watame.command.IntegratedCommands;
}