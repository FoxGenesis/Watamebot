package net.foxgenesis.watame.util;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public abstract class PaginatedMenu extends Menu {
	public static final String ARROW_RIGHT = "\u25B6";
	public static final String ARROW_LEFT = "\u25C0";

	private int currentPage;
	private boolean useIcons = true;

	public PaginatedMenu(long referenceId) {
		super(referenceId);
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public PaginatedMenu setCurrentPage(int page) {
		if (page < 0 || page > getMaxPages())
			throw new IllegalArgumentException();
		this.currentPage = page;
		return this;
	}

	public PaginatedMenu useIcons(boolean state) {
		useIcons = state;
		return this;
	}

	public abstract int getMaxPages();

	public abstract MessageEditData getDataForPage(int page);

	@Override
	public MessageEditData getMenuData() {
		return MessageEditBuilder.from(getDataForPage(currentPage)).setComponents(getControlInputs()).build();
	}

	private ActionRow getControlInputs() {
		return ActionRow.of(
				Button.secondary("pagination:prev", useIcons ? ARROW_LEFT : "Previous").withDisabled(currentPage == 0),
				Button.secondary("pagination:next", useIcons ? ARROW_RIGHT : "Next")
						.withDisabled(currentPage + 1 == getMaxPages()));
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		if (!shouldRespond(event))
			return;

		switch (event.getComponentId()) {
		case "pagination:prev" -> {
			if (currentPage > 0)
				currentPage--;
		}
		case "pagination:next" -> {
			if (currentPage + 1 < getMaxPages())
				currentPage++;
		}
//        case "pagination:stop":
//            // Remove this event listener, to improve performance and free memory
//            // In practice, this should also happen automatically after a certain timeout.
//            event.getJDA().removeEventListener(this);
//            // Using deferEdit marks the component messages the "original"
//            event.deferEdit().queue();
//            // Then delete it using the interaction hook
//            event.getHook().deleteOriginal().queue();
		}

		event.editMessage(getMenuData()).queue();
	}
}
