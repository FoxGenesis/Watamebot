package net.foxgenesis.watame.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public abstract class Menu extends ListenerAdapter {
	private final long referenceId;

	public Menu(long referenceId) {
		this.referenceId = referenceId;
	}

	public boolean shouldRespond(ComponentInteraction interaction) {
		Message.Interaction interactionContext = interaction.getMessage().getInteraction();
		return interactionContext != null && interactionContext.getIdLong() == referenceId;
	}

	public abstract MessageEditData getMenuData();
	
	public MessageCreateData getAsCreateData() {
		return MessageCreateData.fromEditData(getMenuData());
	}

	public long getRefrenceId() {
		return referenceId;
	}
}
