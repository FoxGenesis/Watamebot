package net.foxgenesis.watame.util;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class StaticPaginatedMenu extends PaginatedMenu {
	private final List<MessageEditData> pages = new ArrayList<>();

	public StaticPaginatedMenu(long referenceId) {
		super(referenceId);
	}

	public StaticPaginatedMenu addPage(String content) {
		this.pages.add(MessageEditData.fromContent(content));
		return this;
	}
	
	public StaticPaginatedMenu removePage(int index) {
		pages.remove(index);
		return this;
	}

	@Override
	public int getMaxPages() {
		return pages.size();
	}

	@Override
	public MessageEditData getDataForPage(int page) {
		return pages.get(page);
	}
}
