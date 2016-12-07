package me.dadus33.chatitem.listeners;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;


public class ChatEventListener implements Listener {

    private Storage c;
    public final static char SEPARATOR = ((char)0x0007);
    private final List<String> COOLDOWNS = new ArrayList<>();

    public ChatEventListener(Storage storage) {
        c = storage;
    }


    private int countOccurrences(String findStr, String str){
        int lastIndex = 0;
        int count = 0;
        while(lastIndex != -1){

            lastIndex = str.indexOf(findStr,lastIndex);

            if(lastIndex != -1){
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)  //We need to have lowest priority in order to get to the event before DeluxeChat or other plugins do
    public void onChat(final AsyncPlayerChatEvent e) {
        if(e.getMessage().indexOf(SEPARATOR)!=-1){  //If the BELL character is found, we have to remove it
            String msg = e.getMessage().replace(Character.toString(SEPARATOR), "");
            e.setMessage(msg);
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
                return;
            }
            return;
        }

        if(c.COOLDOWN > 0){
            if(COOLDOWNS.contains(e.getPlayer().getName())){
                e.setCancelled(true);
                if(!c.COOLDOWN_MESSAGE.isEmpty()){
                    e.getPlayer().sendMessage(c.COOLDOWN_MESSAGE);
                }
                return;
            }
            COOLDOWNS.add(e.getPlayer().getName());
            Bukkit.getScheduler().runTaskLater(ChatItem.getInstance(), new Runnable() {
                @Override
                public void run() {
                    COOLDOWNS.remove(e.getPlayer().getName());
                }
            }, c.COOLDOWN*20);
        }

        String s = e.getMessage();
        for(String placeholder : c.PLACEHOLDERS){
            s = s.replace(placeholder, c.PLACEHOLDERS.get(0));
        }
        int occurrences = countOccurrences(c.PLACEHOLDERS.get(0), s);

        if(occurrences>c.LIMIT){
            e.setCancelled(true);
            if(c.LIMIT_MESSAGE.isEmpty()){
                return;
            }
            e.getPlayer().sendMessage(c.LIMIT_MESSAGE);
            return;
        }

        String oldmsg = e.getMessage();
        StringBuilder sb = new StringBuilder(oldmsg);
        sb.append(SEPARATOR).append(e.getPlayer().getName());
        e.setMessage(sb.toString());
        Bukkit.getConsoleSender().sendMessage(String.format(e.getFormat(), e.getPlayer().getDisplayName(), oldmsg));
    }


    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommand(final PlayerCommandPreprocessEvent e){
        if(e.getMessage().indexOf(SEPARATOR)!=-1){  //If the BELL character is found, we have to remove it
            String msg = e.getMessage().replace(Character.toString(SEPARATOR), "");
            e.setMessage(msg);
        }
        Command cmd = Bukkit.getPluginCommand(e.getMessage().split(" ")[0].substring(1));
        if(cmd==null){ //not a plugin command
            return;
        }

        if(!c.ALLOWED_COMMANDS.contains(cmd)){
            return;
        }

        boolean found = false;

        for (String rep : c.PLACEHOLDERS) {
            if (e.getMessage().contains(rep)) {
                found = true;
                break;
            }
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

        if(c.COOLDOWN > 0){
            if(COOLDOWNS.contains(e.getPlayer().getName())){
                e.setCancelled(true);
                if(!c.COOLDOWN_MESSAGE.isEmpty()){
                    e.getPlayer().sendMessage(c.COOLDOWN_MESSAGE);
                }
                return;
            }
            COOLDOWNS.add(e.getPlayer().getName());
            Bukkit.getScheduler().runTaskLater(ChatItem.getInstance(), new Runnable() {
                @Override
                public void run() {
                    COOLDOWNS.remove(e.getPlayer().getName());
                }
            }, c.COOLDOWN*20);
        }

        String s = e.getMessage();
        for(String placeholder : c.PLACEHOLDERS){
            s = s.replace(placeholder, c.PLACEHOLDERS.get(0));
        }
        int occurrences = countOccurrences(c.PLACEHOLDERS.get(0), s);

        if(occurrences>c.LIMIT){
            e.setCancelled(true);
            if(c.LIMIT_MESSAGE.isEmpty()){
               return;
            }
            e.getPlayer().sendMessage(c.LIMIT_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder(e.getMessage());
        sb.append(SEPARATOR).append(e.getPlayer().getName());
        e.setMessage(sb.toString());
    }


    public void setStorage(Storage st) {
        c = st;
    }
}
