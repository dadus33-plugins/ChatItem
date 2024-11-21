package me.dadus33.chatitem.listeners;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.Translation;
import me.dadus33.chatitem.listeners.holder.AdminHolder;
import me.dadus33.chatitem.listeners.holder.ChatItemHolder;
import me.dadus33.chatitem.listeners.holder.CustomInventoryHolder;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.Utils;

public class InventoryListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		Player p = (Player) e.getWhoClicked();

		Inventory topInventory = Utils.getTopInventory(e);
		InventoryHolder openInventoryHolder = topInventory == null ? null : topInventory.getHolder();

		if (openInventoryHolder != null && openInventoryHolder instanceof CustomInventoryHolder) {
			e.setCancelled(true);
			return;
		}
		if (e.getClickedInventory() == null)
			return;

		InventoryHolder holder = e.getClickedInventory().getHolder();
		if (holder == null || !(holder instanceof ChatItemHolder)) {
			if (e.getClick().equals(ClickType.DOUBLE_CLICK) && topInventory != null) {
				if (openInventoryHolder != null && openInventoryHolder instanceof ChatItemHolder) {
					e.setCancelled(true);
				}
			}
			return;
		}
		if (holder instanceof CustomInventoryHolder) {
			e.setCancelled(true);
			return;
		}
		if (!(holder instanceof AdminHolder))
			return;
		e.setCancelled(true);
		if (e.getCurrentItem() == null)
			return;
		ItemStack item = e.getCurrentItem();
		Material type = item.getType();
		Storage c = ChatItem.getInstance().getStorage();
		if (type.equals(ItemUtils.MATERIAL_CLOSE)) {
			p.closeInventory();
		} else if (type.equals(Material.BOOK)) {
			TranslationInventoryListener.open(p, 0);
		} else if (type.equals(Material.PAPER)) {
			String key = ((AdminHolder) holder).keyBySlot.get(e.getSlot());
			if (key != null) {
				setInConfig("manager", key);
				p.closeInventory();
				ChatItem.reload(p);
			}
		} else if (type.equals(ItemUtils.INK_SAC)) {
			setInConfig("general.color-if-already-colored", c.colorIfColored = !c.colorIfColored);
			open(p);
		} else if (type.equals(Material.STICK)) {
			setInConfig("general.hand.disabled", c.handDisabled = !c.handDisabled);
			open(p);
		} else if (type.equals(Material.BLAZE_ROD)) {
			setInConfig("general.check-update", c.checkUpdate = !c.checkUpdate);
			open(p);
		} else if (type.equals(ItemUtils.FIREWORK_CHARGE)) {
			setInConfig("debug", c.debug = !c.debug);
			open(p);
		} else if (type.equals(Material.IRON_DOOR)) {
			if (e.getClick().equals(ClickType.RIGHT))
				c.limit--;
			else if (e.getClick().equals(ClickType.LEFT))
				c.limit++;
			setInConfig("general.limit-per-message", c.limit);
			open(p);
		} else if (type.equals(Material.APPLE)) {
			if (e.getClick().equals(ClickType.RIGHT))
				c.cooldown--;
			else if (e.getClick().equals(ClickType.LEFT))
				c.cooldown++;
			setInConfig("general.cooldown", c.cooldown);
			open(p);
		}
	}

	public static void setInConfig(String key, Object val) {
		ChatItem pl = ChatItem.getInstance();
		pl.getConfig().set(key, val);
		pl.saveConfig();
	}

	public static void open(Player p) {
		AdminHolder holder = new AdminHolder();
		Storage c = ChatItem.getInstance().getStorage();
		Inventory inv = ChatItem.getPlatform().createInventory(holder, 27, Messages.getMessage("admin-inv.name"));
		for (int i = 0; i < inv.getSize(); i++)
			inv.setItem(i, ChatItem.getPlatform().createItemStack(ItemUtils.WHITE_STAINED_GLASS, "-"));

		int slot = 0;
		for (String manager : Arrays.asList("all", "packet", "chat", "paper")) { //, "ownformatter")) {
			if ((manager == "paper" || manager == "ownformatter") && !Utils.IS_PAPER)
				continue;
			holder.keyBySlot.put(slot, manager);
			inv.setItem(slot++, getManagerItem(manager));
		}
		inv.setItem(slot + 1, getManagerItem("actual", "%manager%", Messages.getMessage("admin-inv.manager." + c.manager + ".name")));

		inv.setItem(8, getBoolChangeItem(ItemUtils.FIREWORK_CHARGE, "debug", c.debug));

		inv.setItem(18, getBoolChangeItem(ItemUtils.INK_SAC, "color-if-already-colored", c.colorIfColored));
		inv.setItem(20, getBoolChangeItem(Material.STICK, "hand-disabled", c.handDisabled));
		inv.setItem(21, getAmountChangeItem(Material.IRON_DOOR, "limit-per-message", c.limit));
		inv.setItem(22, getAmountChangeItem(Material.APPLE, "cooldown", c.cooldown));
		inv.setItem(23, getBoolChangeItem(Material.BLAZE_ROD, "check-update", c.checkUpdate));
		inv.setItem(24, ChatItem.getPlatform().createTranslatedItemStack(Material.BOOK, "admin-inv.language", "%name%", Translation.getMessage("language.name")));

		inv.setItem(26, ChatItem.getPlatform().createItemStack(ItemUtils.MATERIAL_CLOSE, Messages.getMessage("admin-inv.close")));
		p.openInventory(inv);
	}

	private static ItemStack getBoolChangeItem(Material type, String key, boolean b) {
		return ChatItem.getPlatform().createItemStack(type, Messages.getMessage("admin-inv." + key, "%state%", Messages.getMessage(b ? "enabled" : "disabled")),
				Messages.getMessageList("admin-inv.bool-lore"));
	}

	private static ItemStack getAmountChangeItem(Material type, String key, int amount) {
		return ChatItem.getPlatform().createItemStack(type, Messages.getMessage("admin-inv." + key, "%state%", amount), Messages.getMessageList("admin-inv.amount-lore"));
	}

	private static ItemStack getManagerItem(String manager, Object... placeholders) {
		return ChatItem.getPlatform().createTranslatedItemStack(Material.PAPER, "admin-inv.manager." + manager, placeholders);
	}
}
