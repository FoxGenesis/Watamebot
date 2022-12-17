module watamebot {
	requires transitive net.dv8tion.jda;
	requires java.logging;
	requires java.sql;
	requires jsr305;
	requires logback.classic;
	requires logback.core;
	requires org.fusesource.jansi;
	requires org.json;
	requires transitive org.slf4j;
	
	exports net.foxgenesis.config;
	exports net.foxgenesis.config.fields;
	exports net.foxgenesis.log;
	exports net.foxgenesis.watame.sql;
	exports net.foxgenesis.watame;
	exports net.foxgenesis.watame.command;
	exports net.foxgenesis.watame.plugin;
	exports net.foxgenesis.util;
	exports net.foxgenesis.util.function;
	exports net.foxgenesis.watame.util;
	
	uses net.foxgenesis.watame.plugin.IPlugin;
	//uses net.foxgenesis.watame.plugin.WatameBotPlugin;
	
	opens resources;
}