package me.dadus33.chatitem.utils;

import me.dadus33.chatitem.ChatItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.logging.Level;

public class General {

    private static Storage c;

    public static void checkConfigVersion() {
        int latestVersion = ChatItem.CFG_VER;
        if (latestVersion != c.CONFIG_VERSION) {
            Bukkit.getLogger().log(Level.WARNING, ChatColor.RED + "ChatItem detected an older or invalid configuration file. Replacing it with the default config...");
            c.performOverwrite();
            Bukkit.getLogger().log(Level.WARNING, ChatColor.RED + "Replacement complete!");
        }
    }

    public static void init(Storage st) {
        c = st;
    }
}
