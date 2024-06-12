package me.dadus33.chatitem.platform.hook;

import java.lang.reflect.Method;
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
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Version;

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

	@Override
	public Version getMinecraftVersion() {
		return Version.getVersionByName(getNMSVersion());
	}
	
	@Override
	public String getNMSVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}
	
	@Override
	public boolean hasBaseComponentSerializer() {
		return getBaseComponentToJsonMethod() != null;
	}
	
	@Override
	public String baseComponentToJson(Object obj) {
		Method m = getBaseComponentToJsonMethod();
		try {
			Object[] args = new Object[m.getParameterCount()];
			args[0] = obj;
			return (String) m.invoke(null, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Object jsonToBaseComponent(String json) {
		Method m = getJsonToBaseComponentMethod();
		try {
			Object[] args = new Object[m.getParameterCount()];
			args[0] = json;
			return m.invoke(null, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Method getBaseComponentToJsonMethod() {
		Class<?> chatSerializerClass = PacketUtils.getNmsClass("IChatBaseComponent$ChatSerializer", "network.chat.", "ChatSerializer", "Component$Serializer");
		Class<?> chatBaseComponentClass = PacketUtils.getNmsClass("IChatBaseComponent", "network.chat.", "Component");
		if(chatSerializerClass == null || chatBaseComponentClass == null)
			return null;
		try {
			for (Method m : chatSerializerClass.getDeclaredMethods()) {
				if(m.getParameterTypes()[0].equals(chatBaseComponentClass) && m.getReturnType().equals(String.class)) {
					return m;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Method getJsonToBaseComponentMethod() {
		Class<?> chatSerializerClass = PacketUtils.getNmsClass("IChatBaseComponent$ChatSerializer", "network.chat.", "ChatSerializer", "Component$Serializer");
		Class<?> chatBaseComponentClass = PacketUtils.getNmsClass("IChatBaseComponent", "network.chat.", "Component");
		if(chatSerializerClass == null || chatBaseComponentClass == null)
			return null;
		try {
			for (Method m : chatSerializerClass.getDeclaredMethods()) {
				if(m.getParameterTypes()[0].equals(String.class) && m.getReturnType().equals(chatBaseComponentClass)) {
					return m;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
