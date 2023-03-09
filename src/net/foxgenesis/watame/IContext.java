package net.foxgenesis.watame;

import net.dv8tion.jda.api.JDA;
import net.foxgenesis.database.DatabaseManager;
import net.foxgenesis.watame.WatameBot.State;
import net.foxgenesis.watame.plugin.EventStore;

public interface IContext {
	DatabaseManager getDatabaseManager();

	EventStore getEventRegister();

	JDA getJDA();
	
	State getState();
}
