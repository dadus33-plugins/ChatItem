package me.dadus33.chatitem.listeners;

import static me.dadus33.chatitem.utils.ItemUtils.createItem;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.Translation;
import me.dadus33.chatitem.listeners.holder.AdminHolder;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;

public class InventoryListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player) || e.getCurrentItem() == null
				|| !(e.getClickedInventory().getHolder() instanceof AdminHolder))
			return;
		e.setCancelled(true);
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		Material type = item.getType();
		Storage c = ChatItem.getInstance().getStorage();
		if(type.equals(ItemUtils.MATERIAL_CLOSE)) {
			p.closeInventory();
		} else if(type.equals(Material.BOOK)) {
			TranslationInventoryListener.open(p, 0);
		} else if(type.equals(Material.PAPER)) {
			AdminHolder holder = (AdminHolder) e.getClickedInventory().getHolder();
			String key = holder.keyBySlot.get(e.getSlot());
			if(key != null) {
				setInConfig("manager", key);
				p.closeInventory();
				ChatItem.reload(p);
			}
		} else if(type.equals(ItemUtils.INK_SAC)) {
			setInConfig("general.color-if-already-colored", c.colorIfColored = !c.colorIfColored);
			open(p);
		} else if(type.equals(Material.REDSTONE)) {
			setInConfig("general.deny-if-no-item", c.denyIfNoItem = !c.denyIfNoItem);
			open(p);
		} else if(type.equals(Material.STICK)) {
			setInConfig("general.hand.disabled", c.handDisabled = !c.handDisabled);
			open(p);
		} else if(type.equals(Material.BLAZE_ROD)) {
			setInConfig("general.check-update", c.checkUpdate = !c.checkUpdate);
			open(p);
		} else if(type.equals(ItemUtils.FIREWORK_CHARGE)) {
			setInConfig("debug", c.debug = !c.debug);
			open(p);
		} else if(type.equals(Material.IRON_DOOR)) {
			if(e.getClick().equals(ClickType.RIGHT))
				c.limit--;
			else if(e.getClick().equals(ClickType.LEFT))
				c.limit++;
			setInConfig("general.limit-per-message", c.limit);
			open(p);
		} else if(type.equals(Material.APPLE)) {
			if(e.getClick().equals(ClickType.RIGHT))
				c.cooldown--;
			else if(e.getClick().equals(ClickType.LEFT))
				c.cooldown++;
			setInConfig("general.cooldown", c.cooldown);
			open(p);
		}
	}
	
	private void setInConfig(String key, Object val) {
		ChatItem pl = ChatItem.getInstance();
		pl.getConfig().set(key, val);
		pl.saveConfig();
	}
	
	public static void open(Player p) {
		AdminHolder holder = new AdminHolder();
		Storage c = ChatItem.getInstance().getStorage();
		Inventory inv = Bukkit.createInventory(holder, 27, Messages.getMessage("admin-inv.name"));
		for(int i = 0; i < inv.getSize(); i ++)
			inv.setItem(i, createItem(ItemUtils.WHITE_STAINED_GLASS, "-"));
		
		int slot = 0;
		for(String manager : Arrays.asList("both", "auto", "packet", "chat")) {
			holder.keyBySlot.put(slot, manager);
			inv.setItem(slot++, getManagerItem(manager));
		}
		inv.setItem(slot + 1, getManagerItem("actual", "%manager%", Messages.getMessage("admin-inv.manager." + c.manager + ".name")));
		
		inv.setItem(8, getBoolChangeItem(ItemUtils.FIREWORK_CHARGE, "debug", c.debug));

		inv.setItem(18, getBoolChangeItem(ItemUtils.INK_SAC, "color-if-already-colored", c.colorIfColored));
		inv.setItem(19, getBoolChangeItem(Material.REDSTONE, "deny-no-item", c.denyIfNoItem));
		inv.setItem(20, getBoolChangeItem(Material.STICK, "hand-disabled", c.handDisabled));
		inv.setItem(21, getAmountChangeItem(Material.IRON_DOOR, "limit-per-message", c.limit));
		inv.setItem(22, getAmountChangeItem(Material.APPLE, "cooldown", c.cooldown));
		inv.setItem(23, getBoolChangeItem(Material.BLAZE_ROD, "check-update", c.checkUpdate));
		inv.setItem(24, createItem(Material.BOOK, Messages.getMessage("admin-inv.language.name"),
				Messages.getMessage("admin-inv.language.lore", "%name%", Translation.getMessages().get("language.name").getAsString())));
		
		inv.setItem(26, createItem(ItemUtils.MATERIAL_CLOSE, Messages.getMessage("admin-inv.close")));
		p.openInventory(inv);
	}
	
	private static ItemStack getBoolChangeItem(Material type, String key, boolean b) {
		return createItem(type, Messages.getMessage("admin-inv." + key, "%state%", Messages.getMessage(b ? "enabled" : "disabled")), Messages.getMessageList("admin-inv.bool-lore"));
	}
	
	private static ItemStack getAmountChangeItem(Material type, String key, int amount) {
		return createItem(type, Messages.getMessage("admin-inv." + key, "%state%", amount), Messages.getMessageList("admin-inv.amount-lore"));
	}
	
	private static ItemStack getManagerItem(String manager, Object... placeholders) {
		return createItem(Material.PAPER, Messages.getMessage("admin-inv.manager." + manager + ".name", placeholders),
				Messages.getMessageList("admin-inv.manager." + manager + ".lore", placeholders));
	}
}
