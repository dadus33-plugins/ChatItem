package me.dadus33.chatitem.listeners.holder;

import java.util.HashMap;

public class TranslationHolder extends ChatItemHolder {
    
	public final HashMap<Integer, String> langBySlot = new HashMap<>();
	private final int page;
	
	public TranslationHolder(int page) {
		this.page = page;
	}
	
	public int getPage() {
		return page;
	}
}
