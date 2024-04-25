package me.dadus33.chatitem.platform.hook;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.platform.IPlatform;
import me.dadus33.chatitem.utils.Colors;

@SuppressWarnings("deprecation")
public class SpigotPlatform implements IPlatform {

	@Override
	public Inventory createInventory(InventoryHolder holder, int slot, String name) {
		return Bukkit.createInventory(holder, slot, name);
	}

	@Override
	public ItemStack createItemStack(Material type, String name) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.setDisplayName(Colors.RESET + name);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public ItemStack createItemStack(Material type, String name, List<String> lore) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.setDisplayName(Colors.RESET + name);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public String getPluginVersion(Plugin plugin) {
		return plugin.getDescription().getVersion();
	}

}
