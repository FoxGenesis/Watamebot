package net.foxgenesis.watame;

import net.dv8tion.jda.api.JDA;
import net.foxgenesis.database.DatabaseManager;

public interface IContext {
	DatabaseManager getDatabaseManager();
	
	JDA getJDA();
}
