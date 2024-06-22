package me.dadus33.chatitem.platform;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.Version;

public interface IPlatform {
	
	String getName();

	Inventory createInventory(InventoryHolder holder, int slot, String name);

	ItemStack createItemStack(Material type, String name);
	
	default ItemStack createTranslatedItemStack(Material type, String key, Object... placeholders) {
		return createItemStack(type, Messages.getMessage(key + ".name", placeholders), Messages.getMessageList(key + ".lore", placeholders));
	}
	
	default ItemStack createItemStack(Material type, String name, String... lore) {
		return createItemStack(type, name, Arrays.asList(lore));
	}
	
	ItemStack createItemStack(Material type, String name, List<String> lore);
	
	String getPluginVersion(Plugin plugin);
	
	Version getMinecraftVersion();
	
	String getNMSVersion();
	
	boolean hasBaseComponentSerializer();
	
	String baseComponentToJson(Object obj);
	
	Object jsonToBaseComponent(String json);
	
	void sendMessage(Player to, Player origin, ChatAction action, String msg);
}
