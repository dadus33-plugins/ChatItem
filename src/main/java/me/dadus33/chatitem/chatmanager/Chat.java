package me.dadus33.chatitem.chatmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemPlayer;
import me.dadus33.chatitem.ItemSlot;
import me.dadus33.chatitem.utils.Utils;

public class Chat {
	
	private static int actualId = 0;
	private static final List<Chat> CHAT = new ArrayList<>();
	public static Optional<Chat> getChat(int id) {
		synchronized (CHAT) {
			CHAT.removeIf(Chat::isOld);
			return CHAT.stream().filter(o -> o.getId() == id).findFirst();
		}
	}
	public static Chat create(Player p, String message, ItemSlot slot) {
		return new Chat(actualId++, p, message, slot);
	}
	public static Chat getFrom(String message) {
		boolean found = false;
		String id = "";
		for(char c : ChatManager.fixSeparator(message).toCharArray()) {
			if(c == ChatManager.SEPARATOR)
				found = true;
			else if(c == ChatManager.SEPARATOR_END)
				break;
			else if(found)
				id += c;
		}
		ChatItem.debug("Search in " + message + ", id found: " + id);
		return (id != "" && Utils.isInteger(id)) ? Chat.getChat(Integer.parseInt(id)).orElse(null) : null;
	}

	private final long time;
	private final int id;
	private final Player p;
	private final String message;
	private final ItemSlot slot;
	
	public Chat(int id, Player p, String message, ItemSlot slot) {
		this.time = System.currentTimeMillis();
		this.id = id;
		this.p = p;
		this.message = message;
		this.slot = slot;
		CHAT.add(this);
	}
	
	public long getTime() {
		return time;
	}
	
	public int getId() {
		return id;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public String getMessage() {
		return message;
	}
	
	public ItemSlot getSlot() {
		return slot;
	}
	
	public ItemPlayer getItemPlayer() {
		return ItemPlayer.getPlayer(getPlayer());
	}
	
	private boolean isOld() {
		return System.currentTimeMillis() > time + 10000;
	}
	
	public void remove() {
		CHAT.remove(this);
	}
	
	@Override
	public String toString() {
		return "Chat{id=" + id + ",message=" + message + ",time=" + time + "}";
	}
}
