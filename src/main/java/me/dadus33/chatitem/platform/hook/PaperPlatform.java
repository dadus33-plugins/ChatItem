package me.dadus33.chatitem.platform.hook;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.platform.IPlatform;
import me.dadus33.chatitem.utils.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PaperPlatform implements IPlatform {

	@Override
	public Inventory createInventory(InventoryHolder holder, int slot, String name) {
		return Bukkit.createInventory(holder, slot, Component.text(name));
	}

	@Override
	public ItemStack createItemStack(Material type, String name) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.displayName(Component.text(name).color(NamedTextColor.WHITE));
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public ItemStack createItemStack(Material type, String name, List<String> lore) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.displayName(Component.text(name).color(NamedTextColor.WHITE));
		meta.lore(lore.stream().map(Component::text).collect(Collectors.toList()));
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public String getPluginVersion(Plugin plugin) {
		return plugin.getPluginMeta().getVersion();
	}
	
	@Override
	public Version getMinecraftVersion() {
		return Version.getVersionByName("v" + Bukkit.getMinecraftVersion().replace(".", "_"));
	}
	
	@Override
	public String getNMSVersion() {
		String[] parts = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",");
		return parts.length <= 3 ? "" : parts[3];
	}
	
	@Override
	public boolean hasBaseComponentSerializer() {
		return Version.getVersion().isNewerOrEquals(Version.V1_20_6) ? false : SpigotPlatform.getBaseComponentToJsonMethod() != null;
	}

	@Override
	public String baseComponentToJson(Object obj) {
		if(Version.getVersion().isNewerOrEquals(Version.V1_20_6))
			return null;
		Method m = SpigotPlatform.getBaseComponentToJsonMethod();
		try {
			return (String) m.invoke(null, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object jsonToBaseComponent(String json) {
		if(Version.getVersion().isNewerOrEquals(Version.V1_20_6))
			return null;
		Method m = SpigotPlatform.getJsonToBaseComponentMethod();
		try {
			return m.invoke(null, json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
