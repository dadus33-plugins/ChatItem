package me.dadus33.chatitem.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.dadus33.chatitem.ChatItem;

public class ItemUtils {

	public static final Material MATERIAL_CLOSE = getMaterialWithCompatibility("BARRIER", "REDSTONE_BLOCK");
	
	// items
	public static final Material EMPTY_MAP = getMaterialWithCompatibility("EMPTY_MAP", "MAP");
	public static final Material BOOK_AND_QUILL = getMaterialWithCompatibility("BOOK_AND_QUILL", "WRITTEN_BOOK");
	public static final Material WEB = getMaterialWithCompatibility("WEB", "COBWEB");
	public static final Material FIREBALL = getMaterialWithCompatibility("FIREBALL", "FIRE_CHARGE");
	public static final Material INK_SAC = getMaterialWithCompatibility("INK_SAC", "INK_SACK", "DYE", "GRAY_DYE");
	public static final Material FIREWORK_CHARGE = getMaterialWithCompatibility("FIREWORK_CHARGE", "FIRE_CHARGE");

	public static final Material BIRCH_WOOD_STAIRS = getMaterialWithCompatibility("BIRCH_WOOD_STAIRS", "BIRCH_STAIRS");
	
	// colored items
	public static final Material BROWN_STAINED_GLASS_PANE = getMaterialWithCompatibility("BROWN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
	public static final Material GRAY_STAINED_GLASS_PANE = getMaterialWithCompatibility("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
	public static final Material WHITE_STAINED_GLASS = getMaterialWithCompatibility("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
	
	public static final ItemStack ITEM_EMPTY_BROWN = ChatItem.getPlatform().createItemStack(BROWN_STAINED_GLASS_PANE, "");
	
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
	
	public static ItemStack hideAttributes(ItemStack stack) {
		if (Version.getVersion().isNewerThan(Version.V1_7)) {
			ItemMeta meta = stack.getItemMeta();
			// All ItemFlags are used to hide attributes, their javadoc says so too.
			meta.addItemFlags(ItemFlag.values());
			stack.setItemMeta(meta);
		}
		return stack;
	}
	
	public static ItemStack copyIfExist(ItemStack item) {
		return isEmpty(item) ? null : item.clone();
	}
	
	public static boolean isEmpty(ItemStack item) {
		return item == null || item.getType().equals(Material.AIR);
	}

	@SuppressWarnings("deprecation")
	public static void stripData(ItemStack i) {
		if (i == null) {
			return;
		}
		if (i.getType().equals(Material.AIR)) {
			return;
		}
		if (!i.hasItemMeta()) {
			return;
		}
		ItemMeta im = Bukkit.getItemFactory().getItemMeta(i.getType());
		ItemMeta original = i.getItemMeta();
		if (original.hasDisplayName()) {
			im.setDisplayName(original.getDisplayName());
		}
		i.setItemMeta(im);
	}
	
	@SuppressWarnings("deprecation")
	public static Enchantment getEnchant(String... names) {
		for(String name : names) {
			Enchantment possible = Enchantment.getByName(name);
			if(possible != null)
				return possible;
		}
		return null;
	}
}
