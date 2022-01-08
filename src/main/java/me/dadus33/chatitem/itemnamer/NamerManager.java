package me.dadus33.chatitem.itemnamer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.itemnamer.INamer.Priority;
import me.dadus33.chatitem.itemnamer.hook.ChatItemTranslationNamer;
import me.dadus33.chatitem.itemnamer.hook.DefaultNamer;
import me.dadus33.chatitem.itemnamer.hook.ItemDisplayNamer;
import me.dadus33.chatitem.itemnamer.hook.LangUtilsNamer;
import me.dadus33.chatitem.utils.Storage;

public class NamerManager {

	private static final HashMap<Priority, List<INamer>> NAMER_PER_PRIORITY = new HashMap<>();

	public static void load(ChatItem pl) {
		NAMER_PER_PRIORITY.clear();
		
		StringJoiner sj = new StringJoiner(", ");
		PluginManager pm = Bukkit.getPluginManager();
		if(pm.getPlugin("LangUtils") != null) {
			addNamer(new LangUtilsNamer());
			sj.add("LangUtils");
		}
		if(sj.length() > 0)
			pl.getLogger().info("Loaded namer for plugin "  + sj.toString() + ".");
		
		addNamer(new ChatItemTranslationNamer());
		addNamer(new ItemDisplayNamer());
		addNamer(new DefaultNamer());
	}
	
	/**
	 * Add namer according to his {@link INamer#getPriority()}'s value
	 * 
	 * @param namer the namer to add
	 */
	public static void addNamer(INamer namer) {
		List<INamer> namers = getOrCreate(namer.getPriority());
		namers.add(namer);
	}
	
	private static List<INamer> getOrCreate(Priority p){
		return NAMER_PER_PRIORITY.computeIfAbsent(p, (a) -> new ArrayList<>());
	}
	
	/**
	 * Get the name of this item, according to this storage.
	 * 
	 * @param item
	 * @param storage
	 * @return the name (should NOT return null)
	 */
	public static String getName(Player p, ItemStack item, Storage storage) {
		for(Priority pr : Priority.getOrderedPriorities()) {
			List<INamer> allNamers = NAMER_PER_PRIORITY.get(pr);
			if(allNamers == null || allNamers.isEmpty())
				continue;
			for(INamer namer : allNamers) {
				String tmpName = namer.getName(p, item, storage);
				if(tmpName != null)
					return tmpName;
			}
		}
		return null; // this should NEVER append
	}
}
