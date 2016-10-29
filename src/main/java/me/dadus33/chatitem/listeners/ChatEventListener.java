package me.dadus33.chatitem.listeners;

import me.dadus33.chatitem.utils.Storage;
import org.bukkit.Material;
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
        String s = e.getMessage();
        for(String placeholder : c.PLACEHOLDERS){
            s = s.replace(placeholder, c.PLACEHOLDERS.get(0));
        }
        int occurrences = countOccurrences(c.PLACEHOLDERS.get(0), s);
        if(occurrences>c.MAX_OCCURRENCES){
            e.setCancelled(true);

        }
        String oldmsg = e.getMessage();
        e.setMessage(e.getMessage().concat(e.getPlayer().getName()));
    }


    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommand(PlayerCommandPreprocessEvent e){
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
        String s = e.getMessage();
        for(String placeholder : c.PLACEHOLDERS){
            s = s.replace(placeholder, c.PLACEHOLDERS.get(0));
        }
        int occurrences = countOccurrences(c.PLACEHOLDERS.get(0), s);
        if(occurrences>c.MAX_OCCURRENCES){
            e.setCancelled(true);

        }

        e.setMessage(e.getMessage().concat(e.getPlayer().getName()));

    }


    public void setStorage(Storage st) {
        c = st;
    }

    private int countOccurrences(String findStr, String str){
        int lastIndex = 0;
        int count = 0;
        while(lastIndex != -1){

            lastIndex = str.indexOf(findStr,lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }

}
