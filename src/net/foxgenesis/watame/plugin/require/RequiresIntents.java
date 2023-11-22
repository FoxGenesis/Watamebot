package net.foxgenesis.watame.plugin.require;

import java.util.EnumSet;

import net.dv8tion.jda.api.requests.GatewayIntent;

public interface RequiresIntents {

	EnumSet<GatewayIntent> getRequiredIntents();
}
