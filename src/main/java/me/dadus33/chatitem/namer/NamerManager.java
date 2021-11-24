package me.dadus33.chatitem.namer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.namer.INamer.Priority;
import me.dadus33.chatitem.namer.hook.ChatItemTranslationNamer;
import me.dadus33.chatitem.namer.hook.DefaultNamer;
import me.dadus33.chatitem.namer.hook.ItemDisplayNamer;
import me.dadus33.chatitem.utils.Storage;

public class NamerManager {

	private static final HashMap<Priority, List<INamer>> NAMER_PER_PRIORITY = new HashMap<>();

	public static void load(ChatItem pl) {
		NAMER_PER_PRIORITY.clear();
		
		StringJoiner sj = new StringJoiner(", ");
		/*PluginManager pm = Bukkit.getPluginManager();
		if(pm.getPlugin("LangUtils") != null) {
			addNamer(new LangUtilsNamer());
			sj.add("LangUtils");
		}*/
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
	public static String getName(ItemStack item, Storage storage) {
		for(Priority p : Priority.getOrderedPriorities()) {
			List<INamer> allNamers = NAMER_PER_PRIORITY.get(p);
			if(allNamers == null || allNamers.isEmpty())
				continue;
			for(INamer namer : allNamers) {
				String tmpName = namer.getName(item, storage);
				if(tmpName != null)
					return tmpName;
			}
		}
		return null; // this should NEVER append
	}
}
