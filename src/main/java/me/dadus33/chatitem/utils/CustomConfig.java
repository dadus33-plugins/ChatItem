package me.dadus33.chatitem.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Copyright (C) 2016 Vlad Ardelean - All Rights Reserved
 * You are not allowed to edit, modify or
 * decompile the contents of this file and/or
 * any other file found in the enclosing jar
 * unless explicitly permitted by me.
 * Written by Vlad Ardelean <LongLiveVladerius@gmail.com>
 */

public class CustomConfig {
    Plugin plugin;

    public CustomConfig(Plugin instance) {
        plugin = instance;
    }

    public FileConfiguration getCustomConfig(Config config) {
        if (config.fileConfig == null) {
            reloadCustomConfig(config);
        }
        return config.fileConfig;
    }

    public void reloadCustomConfig(Config config) {
        if (config.fileConfig == null) {
            config.file = new File(plugin.getDataFolder(), config.name + ".yml");
        }
        config.fileConfig = YamlConfiguration.loadConfiguration(config.file);

        InputStream defConfigStream = plugin.getResource(config.name + ".yml");
        if (defConfigStream != null) {
            @SuppressWarnings("deprecation")
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.fileConfig.setDefaults(defConfig);
        }
    }

    public void saveCustomConfig(Config config) {
        if (config.fileConfig == null || config.file == null) {
            return;
        }
        try {
            getCustomConfig(config).save(config.file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + config.file, ex);
        }
    }

    public void saveDefaultConfig(Config config) {
        if (config.file == null) {
            config.file = new File(plugin.getDataFolder(), config.name + ".yml");
        }
        if (!config.file.exists()) {
            plugin.saveResource(config.name + ".yml", true);
        }
    }


    public void overwriteWithDefautConfig(Config config) {
        plugin.saveResource(config.name + ".yml", true);
    }


}