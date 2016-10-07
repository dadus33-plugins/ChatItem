package me.dadus33.chatitem.listeners;

import me.dadus33.chatitem.utils.Storage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;


public class ChatEventListener implements Listener {

    Storage c;

    public ChatEventListener(Storage storage) {
        c = storage;
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!e.isAsynchronous()) {
            return;
        }
        boolean found = false;
        for (String rep : c.PLACEHOLDERS)
            if (e.getMessage().contains(rep)) {
                found = true;
                break;
            }
        if (!found) {
            return;
        }
        ChatPacketListener.SENDERS.put(System.currentTimeMillis(), e.getPlayer().getName());

    }

    public void setStorage(Storage st) {
        c = st;
    }
}
