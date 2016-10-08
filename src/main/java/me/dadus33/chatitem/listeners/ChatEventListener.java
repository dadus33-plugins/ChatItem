package me.dadus33.chatitem.listeners;

import me.dadus33.chatitem.utils.Storage;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;


public class ChatEventListener implements Listener {

    private Storage c;

    public ChatEventListener(Storage storage) {
        c = storage;
    }


    @SuppressWarnings("deprecation")
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
        if (!e.getPlayer().hasPermission("chatitem.use")) {
            e.setCancelled(true);
            return;
        }
        if (e.getPlayer().getItemInHand().getType().equals(Material.AIR)) {
            if (c.DENY_IF_NO_ITEM) {
                e.setCancelled(true);
                if (!c.DENY_MESSAGE.isEmpty())
                    e.getPlayer().sendMessage(c.DENY_MESSAGE);
            }
            return;
        }
        Long curr = System.currentTimeMillis();
        ChatPacketListener.SENDERS.put(curr, e.getPlayer().getName());

    }


    public void setStorage(Storage st) {
        c = st;
    }
}
