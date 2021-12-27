package me.dadus33.chatitem.chatmanager.v1.json;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public interface JSONManipulator {
	
    String parse(String json, List<String> replacements, ItemStack item, String repl, int protocol) throws Exception;
    String parseEmpty(String json, List<String> replacements, String repl, List<String> tooltip, Player sender);
    
}
