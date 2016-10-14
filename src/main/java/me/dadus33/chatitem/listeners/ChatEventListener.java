package me.dadus33.chatitem.listeners;

import me.dadus33.chatitem.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class ChatEventListener implements Listener {

    private Storage c;

    public ChatEventListener(Storage storage) {
        c = storage;
    }


    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
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
                return;
            }
            return;
        }
        String oldmsg = e.getMessage();
        e.setMessage(e.getMessage().concat(e.getPlayer().getName()));
        Bukkit.getConsoleSender().sendMessage(String.format(e.getFormat(), e.getPlayer().getDisplayName(), oldmsg));
    }


    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommand(PlayerCommandPreprocessEvent e){
        Command cmd = Bukkit.getPluginCommand(e.getMessage().split(" ")[0].substring(1));
        if(!c.ALLOWED_COMMANDS.contains(cmd)){
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

        e.setMessage(e.getMessage().concat(e.getPlayer().getName()));

    }


    public void setStorage(Storage st) {
        c = st;
    }
}
