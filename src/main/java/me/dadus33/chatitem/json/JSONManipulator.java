package me.dadus33.chatitem.json;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.List;


public interface JSONManipulator {
    String parse(String json, List<String> replacements, ItemStack item, String repl) throws InvocationTargetException, IllegalAccessException, InstantiationException;
    String parseEmpty(String json, List<String> replacements, String repl, List<String> tooltip, Player sender);
}
