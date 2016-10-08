package me.dadus33.chatitem.utils;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Copyright (C) 2016 Vlad Ardelean - All Rights Reserved
 * You are not allowed to edit, modify or
 * decompile the contents of this file and/or
 * any other file found in the enclosing jar
 * unless explicitly permitted by me.
 * Written by Vlad Ardelean <LongLiveVladerius@gmail.com>
 */

public class Storage {

    public final HashMap<String, HashMap<Short, String>> TRANSLATIONS = new HashMap<>();
    public final ImmutableList<String> PLACEHOLDERS;
    public final String NAME_FORMAT;
    public final String AMOUNT_FORMAT;
    public final Boolean COLOR_IF_ALREADY_COLORED;
    public final Boolean FORCE_ADD_AMOUNT;
    public final Boolean DENY_IF_NO_ITEM;
    public final String DENY_MESSAGE;
    public final String RELOAD_MESSAGE;
    final Integer CONFIG_VERSION;
    private final Config cfg;
    private final CustomConfig handler;
    private final FileConfiguration conf;


    public Storage(Config cfg, CustomConfig handler) {
        this.cfg = cfg;
        this.handler = handler;
        this.conf = handler.getCustomConfig(cfg);
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
        List<String> added = conf.getStringList("placeholders");
        PLACEHOLDERS = ImmutableList.copyOf(added);
        NAME_FORMAT = color(conf.getString("name-format"));
        AMOUNT_FORMAT = color(conf.getString("amount-format"));
        COLOR_IF_ALREADY_COLORED = conf.getBoolean("color-if-already-colored");
        FORCE_ADD_AMOUNT = conf.getBoolean("force-add-amount");
        DENY_IF_NO_ITEM = conf.getBoolean("deny-if-no-item");
        DENY_MESSAGE = color(conf.getString("deny-message"));
        RELOAD_MESSAGE = color(conf.getString("reload-success"));
    }


    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static List<String> colorList(List<String> ls) {
        List<String> ret = new ArrayList<>();
        for (String s : ls) {
            ret.add(color(s));
        }
        return ret;
    }

    void performOverwrite() {
        handler.overwriteWithDefautConfig(cfg);
    }

}
