package me.dadus33.chatitem.utils;

import com.google.common.collect.ImmutableList;
import me.dadus33.chatitem.ChatItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;


public class Storage {

    public final HashMap<String, HashMap<Short, String>> TRANSLATIONS = new HashMap<>();
    public final List<String> PLACEHOLDERS;
    public final String NAME_FORMAT;
    public final String AMOUNT_FORMAT;
    public final Boolean COLOR_IF_ALREADY_COLORED;
    public final Boolean FORCE_ADD_AMOUNT;
    public final Boolean DENY_IF_NO_ITEM;
    public final String DENY_MESSAGE;
    public final String LIMIT_MESSAGE;
    public final String RELOAD_MESSAGE;
    final Integer CONFIG_VERSION;
    public final Integer LIMIT;
    public final List<Command> ALLOWED_COMMANDS = new ArrayList<>();

    public Storage(FileConfiguration conf) {
        Set<String> keys = conf.getConfigurationSection("Translations").getKeys(false);
        for (String key : keys) {
            HashMap<Short, String> entry = new HashMap<>();
            Set<String> subKeys = conf.getConfigurationSection("Translations.".concat(key)).getKeys(false);
            for (String subKey : subKeys) {
                entry.put(Short.parseShort(subKey), color(conf.getString("Translations.".concat(key).concat(".")
                        .concat(subKey))));
            }

            TRANSLATIONS.put(key, entry);
        }
        CONFIG_VERSION = conf.getInt("config-version");
        LIMIT = conf.getInt("limit");
        List<String> added = conf.getStringList("placeholders");
        PLACEHOLDERS = ImmutableList.copyOf(added);
        NAME_FORMAT = color(conf.getString("name-format"));
        AMOUNT_FORMAT = color(conf.getString("amount-format"));
        COLOR_IF_ALREADY_COLORED = conf.getBoolean("color-if-already-colored");
        FORCE_ADD_AMOUNT = conf.getBoolean("force-add-amount");
        DENY_IF_NO_ITEM = conf.getBoolean("deny-if-no-item");
        DENY_MESSAGE = color(conf.getString("deny-message"));
        LIMIT_MESSAGE = color(conf.getString("limit-message"));
        RELOAD_MESSAGE = color(conf.getString("reload-success"));
        final List<String> cmds = conf.getStringList("commands");
        Bukkit.getScheduler().runTaskLaterAsynchronously(ChatItem.getInstance(), new Runnable() {
            @Override
            public void run() {
                for(String s : cmds){
                    Command c = Bukkit.getPluginCommand(s);
                    if(c!=null) {
                        ALLOWED_COMMANDS.add(Bukkit.getPluginCommand(s));
                    }
                    else
                        ChatItem.getInstance().getLogger().log(Level.WARNING, ChatColor.RED.toString().concat("Unknown command ")
                                .concat(s).concat(" found in config! Ignoring occurrence..."));
                }
            }
        }, 100L);

    }


    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }


    void performOverwrite() {
        ChatItem.getInstance().saveResource("config.yml", true);
    }

}
