package me.dadus33.chatitem.utils;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

	public static final Material MATERIAL_CLOSE = getMaterialWithCompatibility("BARRIER", "REDSTONE_BLOCK");
	
	// items
	public static final Material EMPTY_MAP = getMaterialWithCompatibility("EMPTY_MAP", "MAP");
	public static final Material BOOK_AND_QUILL = getMaterialWithCompatibility("BOOK_AND_QUILL", "WRITTEN_BOOK");
	public static final Material WEB = getMaterialWithCompatibility("WEB", "COBWEB");
	public static final Material FIREBALL = getMaterialWithCompatibility("FIREBALL", "FIRE_CHARGE");
	public static final Material INK_SAC = getMaterialWithCompatibility("INK_SAC", "INK_SACK", "DYE", "GRAY_DYE");

	public static final Material BIRCH_WOOD_STAIRS = getMaterialWithCompatibility("BIRCH_WOOD_STAIRS", "BIRCH_STAIRS");
	
	// colored items
	public static final Material GRAY_STAINED_GLASS_PANE = getMaterialWithCompatibility("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE");
	public static final Material WHITE_STAINED_GLASS = getMaterialWithCompatibility("STAINED_GLASS_PANE", "WHITE_STAINED_GLASS_PANE");
	
	public static Material getMaterialWithCompatibility(String... tempMat) {
		for(String s : tempMat) {
			try {
				Material m = (Material) Material.class.getField(s).get(Material.class);
				if(m != null)
					return m;
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException e2) {
				e2.printStackTrace();
			} catch (NoSuchFieldException e) {}
		}
		return null;
	}

	public static ItemStack createItem(Material m, String name, String... lore) {
		return createItem(m, name, 1, lore);
	}

	public static ItemStack createItem(Material m, String name, int quantite, String... lore) {
		ItemStack item = new ItemStack(m, quantite);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + name);
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack createItem(Material m, String name, List<String> lore) {
		return createItem(m, name, 1, lore);
	}

	public static ItemStack createItem(Material m, String name, int quantite, List<String> lore) {
		ItemStack item = new ItemStack(m, quantite);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + name);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack createItem(Material m, String name, int amount, byte b, String... lore) {
		ItemStack item = new ItemStack(m, amount, b);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + name);
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		return item;
	}
	
	public static ItemStack hideAttributes(ItemStack stack) {
		if (Version.getVersion().isNewerThan(Version.V1_7)) {
			ItemMeta meta = stack.getItemMeta();
			// All ItemFlags are used to hide attributes, their javadoc says so too.
			meta.addItemFlags(ItemFlag.values());
			stack.setItemMeta(meta);
		}
		return stack;
	}
}
