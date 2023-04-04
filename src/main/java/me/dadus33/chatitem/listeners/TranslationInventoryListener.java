package me.dadus33.chatitem.listeners;

import static me.dadus33.chatitem.utils.ItemUtils.createItem;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Translation;
import me.dadus33.chatitem.listeners.holder.TranslationHolder;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;

public class TranslationInventoryListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player) || e.getCurrentItem() == null
				|| !(e.getClickedInventory().getHolder() instanceof TranslationHolder))
			return;
		e.setCancelled(true);
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		Material type = item.getType();
		TranslationHolder holder = (TranslationHolder) e.getClickedInventory().getHolder();
		if(type.equals(Material.ARROW)) {
			if(e.getSlot() == 0)
				InventoryListener.open(p);
			else if(e.getSlot() == 3)
				open(p, holder.getPage() - 1);
			else if(e.getSlot() == 5)
				open(p, holder.getPage() + 1);
		} else if(type.equals(ItemUtils.MATERIAL_CLOSE))
			p.closeInventory();
		else if(type.equals(Material.PAPER)) {
			String lang = holder.langBySlot.get(e.getSlot());
			if(lang == null)
				return;
			p.closeInventory();
			ChatItem pl = ChatItem.getInstance();
			pl.getStorage().language = lang;
			pl.getConfig().set("general.language", lang);
			pl.saveConfig();
			Translation.loadLang(lang);
			p.sendMessage(ChatColor.GREEN + "Language changed to " + Translation.getMessages().get("language.name").getAsString());
		}
	}

	
	public static void open(Player p, int page) {
		int perPage = 45;
		List<String> langs = new ArrayList<>(Translation.getAllLangs().keySet());
		TranslationHolder holder = new TranslationHolder(page);
		Inventory inv = Bukkit.createInventory(holder, 54, "Change language - " + ChatColor.GOLD + " Page " + (page + 1) + " / " + ((langs.size() / perPage) + 1));
		for(int i = 0; i < 9; i ++)
			inv.setItem(i, new ItemStack(ItemUtils.GRAY_STAINED_GLASS_PANE));
		inv.setItem(0, createItem(Material.ARROW, ChatColor.GRAY + "Back"));

		if(page > 0)
			inv.setItem(3, createItem(Material.ARROW, ChatColor.RED + "Previous page"));
		if(langs.size() > (perPage * (page + 1)))
			inv.setItem(5, createItem(Material.ARROW, ChatColor.GREEN + "Next page"));
		
		inv.setItem(8, createItem(ItemUtils.MATERIAL_CLOSE, Messages.getMessage("admin-inv.close")));
		
		int slot = 9;
		for(int i = (page * perPage); i < langs.size() && (i < ((page + 1) * perPage)); i++) {
			String lang = langs.get(i);
			holder.langBySlot.put(slot, lang);
			ItemStack item = createItem(Material.PAPER, ChatColor.GOLD + Translation.getAllLangs().get(lang), ChatColor.GRAY + "Clic to select this language");
			if(lang.equalsIgnoreCase(ChatItem.getInstance().getStorage().language)) {
				item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
				ItemMeta meta = item.getItemMeta();
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				item.setItemMeta(meta);
			}
			inv.setItem(slot++, item);
		}
		
		p.openInventory(inv);
	}
}
