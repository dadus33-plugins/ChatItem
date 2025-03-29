package me.dadus33.chatitem.platform.hook;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.C;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.platform.IPlatform;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.ReflectionUtils;
import me.dadus33.chatitem.utils.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;

public class PaperPlatform implements IPlatform {

	@Override
	public String getName() {
		return "PaperMC";
	}

	@Override
	public Inventory createInventory(InventoryHolder holder, int slot, String name) {
		return Bukkit.createInventory(holder, slot, C.text(name));
	}

	@Override
	public ItemStack createItemStack(Material type, String name) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.displayName(C.text(name).color(NamedTextColor.WHITE));
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public ItemStack createItemStack(Material type, String name, List<String> lore) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.displayName(C.text(name).color(NamedTextColor.WHITE));
		meta.lore(lore.stream().map(C::text).collect(Collectors.toList()));
		item.setItemMeta(meta);
		return item;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getItemDisplayName(ItemStack item) {
		// actually the get Displayname for better ... See:
		// https://github.com/KyoriPowered/adventure-platform/issues/185
		// return
		// LegacyComponentSerializer.legacySection().serialize(item.displayName());
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

	private Object getRegistry() throws Exception {
		Class<?> serverClass = Class.forName("net.minecraft.server.MinecraftServer");
		return serverClass.getDeclaredMethod("registryAccess").invoke(serverClass.getDeclaredMethod("getServer").invoke(null));
	}
	
	@Override
	public String baseComponentToJson(Object obj) {
		Method m = SpigotPlatform.getBaseComponentToJsonMethod();
		if (m == null)
			return null;
		try {
			Object[] args = new Object[m.getParameterCount()];
			args[0] = obj;
			if (args.length > 1 && ReflectionUtils.isClassExist("net.minecraft.core.HolderLookup$Provider")) {
				Class<?> c = Class.forName("net.minecraft.core.HolderLookup$Provider");
				if (m.getParameterTypes()[1].isAssignableFrom(c)) {
					args[1] = getRegistry();
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
			if (args.length > 1 && ReflectionUtils.isClassExist("net.minecraft.core.HolderLookup$Provider")) {
				Class<?> c = Class.forName("net.minecraft.core.HolderLookup$Provider");
				if (m.getParameterTypes()[1].isAssignableFrom(c)) {
					args[1] = getRegistry();
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
		HoverEventSource<?> hoverEvent = null;
		if (action.hasItem()) {
			if (!action.getItem().getType().equals(Material.AIR))
				hoverEvent = action.getItem().asHoverEvent();
			else {
				Component t = null;
				for (String line : Messages.getMessageList("general.hand.tooltip", "%cible%", origin.getName())) {
					if (t == null) {
						t = Component.text("");
					} else
						t.append(Component.newline());
					t.append(C.text(line));
				}
				hoverEvent = HoverEvent.showText(t);
			}
		} else
			hoverEvent = HoverEvent.showText(C.text(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", origin.getName())));
		TextComponent like = Component.text(ChatManager.getNameForChatAction(origin, action, ChatItem.getInstance().getStorage())).hoverEvent(hoverEvent);
		if (action.hasCommand())
			like = like.clickEvent(ClickEvent.runCommand(action.getCommand()));

		to.sendMessage(C.text(msg).replaceText(TextReplacementConfig.builder().matchLiteral(ChatManager.SEPARATOR + "").replacement(like).build()));
	}

	@SuppressWarnings("deprecation")
	@Override
	public String stringifyItem(ItemStack item) {
		ShowItem si = item.asHoverEvent().value();
		String json = "{id:\"" + si.item().asString() + "\",count:" + item.getAmount();
		if(si.nbt() != null) {
			json += ",tag:{" + si.nbt().string() + "}";
		} else if(Version.getVersion().isNewerOrEquals(Version.V1_20_6)) { // since MC 1.20.5 in fact
			json += ",components:" + (item.hasItemMeta() ? item.getItemMeta().getAsString() : "{}");
		}
		json += "}";
		ChatItem.debug("Item stringified: " + json);
		return json;
	}
}
