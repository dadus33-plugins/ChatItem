package me.dadus33.chatitem.listeners;

import me.dadus33.chatitem.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;


public class ChatEventListener implements Listener {

    private Storage c;
    public final static char SEPARATOR = ((char)0x0007);
    private final static String LEFT = "{remaining}";
    private final HashMap<String, Long> COOLDOWNS = new HashMap<>();

    public ChatEventListener(Storage storage) {
        c = storage;
    }

    private String calculateTime(long seconds){
        if(seconds < 60){
            return seconds+c.SECONDS;
        }
        if(seconds < 3600){
            StringBuilder builder = new StringBuilder();
            int minutes = (int) seconds / 60;
            builder.append(minutes).append(c.MINUTES);
            int secs = (int) seconds - minutes*60;
            if(secs != 0){
                builder.append(" ").append(secs).append(c.SECONDS);
            }
            return builder.toString();
        }
        StringBuilder builder = new StringBuilder();
        int hours = (int) seconds / 3600;
        builder.append(hours).append(c.HOURS);
        int minutes = (int) (seconds/60) - (hours*60);
        if(minutes != 0){
            builder.append(" ").append(minutes).append(c.MINUTES);
        }
        int secs = (int) (seconds - ((seconds/60)*60));
        if(secs != 0){
            builder.append(" ").append(secs).append(c.SECONDS);
        }
        return builder.toString();
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

        Player p = e.getPlayer();

        if (!p.hasPermission("chatitem.use")) {
            if(!c.LET_MESSAGE_THROUGH)
                e.setCancelled(true);
            return;
        }

        if (p.getItemInHand().getType().equals(Material.AIR)) {
            if (c.DENY_IF_NO_ITEM) {
                e.setCancelled(true);
                if (!c.DENY_MESSAGE.isEmpty())
                    e.getPlayer().sendMessage(c.DENY_MESSAGE);
                return;
            }
            if(c.HAND_DISABLED) {
                return;
            }
        }

        if(c.COOLDOWN > 0 && !p.hasPermission("chatitem.ignore-cooldown")){
            if(COOLDOWNS.containsKey(p.getName())){
                long start = COOLDOWNS.get(p.getName());
                long current = System.currentTimeMillis()/1000;
                long elapsed = current - start;
                if(elapsed >= c.COOLDOWN){
                    COOLDOWNS.remove(p.getName());
                }else{
                    if(!c.LET_MESSAGE_THROUGH) {
                        e.setCancelled(true);
                    }
                    if(!c.COOLDOWN_MESSAGE.isEmpty()){
                        long left = (start + c.COOLDOWN) - current;
                        p.sendMessage(c.COOLDOWN_MESSAGE.replace(LEFT, calculateTime(left)));
                    }
                    return;
                }
            }
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
        if(!p.hasPermission("chatitem.ignore-cooldown")) {
            COOLDOWNS.put(p.getName(), System.currentTimeMillis() / 1000);
        }
    }


    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(final PlayerCommandPreprocessEvent e){
        if(e.getMessage().indexOf(SEPARATOR)!=-1){  //If the BELL character is found, we have to remove it
            String msg = e.getMessage().replace(Character.toString(SEPARATOR), "");
            e.setMessage(msg);
        }
        String commandString = e.getMessage().split(" ")[0].replaceAll("^/+", ""); //First part of the command, without leading slashes and without arguments
        Command cmd = Bukkit.getPluginCommand(commandString);
        if(cmd==null){ //not a plugin command
            if(!c.ALLOWED_DEFAULT_COMMANDS.contains(commandString)){
                return;
            }
        }else{
            if(!c.ALLOWED_PLUGIN_COMMANDS.contains(cmd)){
                return;
            }
        }

        Player p = e.getPlayer();
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

        if (!p.hasPermission("chatitem.use")) {
            e.setCancelled(true);
            return;
        }

        if (e.getPlayer().getItemInHand().getType().equals(Material.AIR)) {
            if (c.DENY_IF_NO_ITEM) {
                e.setCancelled(true);
                if (!c.DENY_MESSAGE.isEmpty()) {
                    e.getPlayer().sendMessage(c.DENY_MESSAGE);
                }
            }
            if(c.HAND_DISABLED) {
                return;
            }
        }

        if(c.COOLDOWN > 0 && !p.hasPermission("chatitem.ignore-cooldown")){
            if(COOLDOWNS.containsKey(p.getName())){
                long start = COOLDOWNS.get(p.getName());
                long current = System.currentTimeMillis()/1000;
                long elapsed = current - start;
                if(elapsed >= c.COOLDOWN){
                    COOLDOWNS.remove(p.getName());
                }else{
                    if(!c.LET_MESSAGE_THROUGH) {
                        e.setCancelled(true);
                    }
                    if(!c.COOLDOWN_MESSAGE.isEmpty()){
                        long left = (start + c.COOLDOWN) - current;
                        p.sendMessage(c.COOLDOWN_MESSAGE.replace(LEFT, calculateTime(left)));
                    }
                    return;
                }
            }
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
        if(!p.hasPermission("chatitem.ignore-cooldown")) {
            COOLDOWNS.put(p.getName(), System.currentTimeMillis() / 1000);
        }
    }


    public void setStorage(Storage st) {
        c = st;
    }
}
