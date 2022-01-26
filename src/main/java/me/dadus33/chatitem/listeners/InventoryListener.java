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
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.Storage;

public class InventoryListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player) || e.getCurrentItem() == null
				|| !(e.getClickedInventory().getHolder() instanceof ChatItemAdminHolder))
			return;
		e.setCancelled(true);
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		Material type = item.getType();
		Storage c = ChatItem.getInstance().getStorage();
		if(type.equals(ItemUtils.MATERIAL_CLOSE)) {
			p.closeInventory();
		} else if(type.equals(Material.PAPER)) {
			ChatItemAdminHolder holder = (ChatItemAdminHolder) e.getClickedInventory().getHolder();
			String key = holder.keyBySlot.get(e.getSlot());
			if(key != null) {
				setInConfig("manager", key);
				p.closeInventory();
				ChatItem.reload(p);
			}
		} else if(type.equals(ItemUtils.INK_SAC)) {
			setInConfig("general.color-if-already-colored", c.COLOR_IF_ALREADY_COLORED = !c.COLOR_IF_ALREADY_COLORED);
			open(p);
		} else if(type.equals(Material.REDSTONE)) {
			setInConfig("general.deny-if-no-item", c.DENY_IF_NO_ITEM = !c.DENY_IF_NO_ITEM);
			open(p);
		} else if(type.equals(Material.STICK)) {
			setInConfig("general.hand.disabled", c.HAND_DISABLED = !c.HAND_DISABLED);
			open(p);
		} else if(type.equals(Material.IRON_DOOR)) {
			if(e.getClick().equals(ClickType.RIGHT))
				c.LIMIT--;
			else if(e.getClick().equals(ClickType.LEFT))
				c.LIMIT++;
			setInConfig("general.limit-per-message", c.LIMIT);
			open(p);
		} else if(type.equals(Material.APPLE)) {
			if(e.getClick().equals(ClickType.RIGHT))
				c.COOLDOWN--;
			else if(e.getClick().equals(ClickType.LEFT))
				c.COOLDOWN++;
			setInConfig("general.cooldown", c.COOLDOWN);
			open(p);
		}
	}
	
	private void setInConfig(String key, Object val) {
		ChatItem pl = ChatItem.getInstance();
		pl.getConfig().set(key, val);
		pl.saveConfig();
	}
	
	public static void open(Player p) {
		ChatItemAdminHolder holder = new ChatItemAdminHolder();
		Storage c = ChatItem.getInstance().getStorage();
		Inventory inv = Bukkit.createInventory(holder, 27, Messages.getMessage("admin-inv.name"));
		for(int i = 0; i < inv.getSize(); i ++)
			inv.setItem(i, createItem(ItemUtils.WHITE_STAINED_GLASS, "-"));
		
		int slot = 0;
		for(String manager : Arrays.asList("both", "auto", "packet", "chat")) {
			holder.keyBySlot.put(slot, manager);
			inv.setItem(slot++, getManagerItem(manager));
		}
		inv.setItem(slot + 1, getManagerItem("actual", "%manager%", Messages.getMessage("admin-inv.manager." + c.MANAGER + ".name")));

		inv.setItem(18, getBoolChangeItem(ItemUtils.INK_SAC, "color-if-already-colored", c.COLOR_IF_ALREADY_COLORED));
		inv.setItem(19, getBoolChangeItem(Material.REDSTONE, "deny-no-item", c.DENY_IF_NO_ITEM));
		inv.setItem(20, getBoolChangeItem(Material.STICK, "hand-disabled", c.HAND_DISABLED));
		inv.setItem(21, getAmountChangeItem(Material.IRON_DOOR, "limit-per-message", c.LIMIT));
		inv.setItem(22, getAmountChangeItem(Material.APPLE, "cooldown", c.COOLDOWN));
		
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
