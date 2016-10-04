package me.dadus33.chatitem.utils;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

/**
 * Copyright (C) 2016 Vlad Ardelean - All Rights Reserved
 * You are not allowed to edit, modify or
 * decompile the contents of this file and/or
 * any other file found in the enclosing jar
 * unless explicitly permitted by me.
 * Written by Vlad Ardelean <LongLiveVladerius@gmail.com>
 */

public class Config {
    public String name;
    public File file;
    public FileConfiguration fileConfig;

    public Config(String name) {
        this.name = name;
    }
}