package me.dadus33.chatitem.platform.hook;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.platform.IPlatform;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.ReflectionUtils;
import me.dadus33.chatitem.utils.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PaperPlatform implements IPlatform {

	@Override
	public String getName() {
		return "PaperMC";
	}

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
	
	@SuppressWarnings("deprecation")
	@Override
	public String getItemDisplayName(ItemStack item) {
		// actually the get Displayname for better ... See: https://github.com/KyoriPowered/adventure-platform/issues/185
		// return LegacyComponentSerializer.legacySection().serialize(item.displayName());
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
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
		return SpigotPlatform.getBaseComponentToJsonMethod() != null;
	}

	@Override
	public String baseComponentToJson(Object obj) {
		Method m = SpigotPlatform.getBaseComponentToJsonMethod();
		if (m == null)
			return null;
		try {
			Object[] args = new Object[m.getParameterCount()];
			args[0] = obj;
			if(args.length > 1 && ReflectionUtils.isClassExist("net.minecraft.core.HolderLookup$Provider")) {
				Class<?> c = Class.forName("net.minecraft.core.HolderLookup$Provider");
				if(m.getParameterTypes()[1].isAssignableFrom(c)) {
					args[1] = c.getDeclaredMethod("create", Stream.class).invoke(null, Stream.of());
				}
			}
			return (String) m.invoke(null, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Object jsonToBaseComponent(String json) {
		Method m = SpigotPlatform.getJsonToBaseComponentMethod();
		try {
			Object[] args = new Object[m.getParameterCount()];
			args[0] = json;
			if(args.length > 1 && ReflectionUtils.isClassExist("net.minecraft.core.HolderLookup$Provider")) {
				Class<?> c = Class.forName("net.minecraft.core.HolderLookup$Provider");
				if(m.getParameterTypes()[1].isAssignableFrom(c)) {
					args[1] = c.getDeclaredMethod("create", Stream.class).invoke(null, Stream.of());
				}
			}
			return m.invoke(null, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void sendMessage(Player to, Player origin, ChatAction action, String msg) {
		Component comp = LegacyComponentSerializer.legacySection().deserialize(msg);
		ComponentLike like = LegacyComponentSerializer.legacySection().deserialize(ChatManager.getNameForChatAction(origin, action, ChatItem.getInstance().getStorage())).hoverEvent(action.isItem() ? action.getItem().asHoverEvent()
				: HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", origin.getName()))));

		to.sendMessage(comp.replaceText(TextReplacementConfig.builder().matchLiteral(ChatManager.SEPARATOR + "").replacement(like).build()));
	}
}
