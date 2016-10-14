package me.dadus33.chatitem.json;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;


public interface JSONManipulator {
    String parse(String json, String[] replacements, ItemStack item, String repl) throws InvocationTargetException, IllegalAccessException, InstantiationException;
}
