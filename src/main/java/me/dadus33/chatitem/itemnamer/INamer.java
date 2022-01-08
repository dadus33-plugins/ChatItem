package me.dadus33.chatitem.itemnamer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.utils.Storage;

public interface INamer {
	
	/**
	 * Get priority of this namer.<br>
	 * Get first name that is not null, with this order:
	 * - MAJOR
	 * 
	 * 
	 * @return priority
	 */
	Priority getPriority();
	
	/**
	 * Get the name of the item thanks to this namer.
	 * 
	 * @param item the item to get the name
	 * @param storage the actual config
	 * @return the name or null if can't find a name
	 */
	String getName(Player p, ItemStack item, Storage storage);
	
	public static enum Priority {
		MAJOR(4),
		IMPORTANT(3),
		MEDIUM(2),
		SMALL(1),
		MINOR(0);
		
		private final int priority;
		
		private Priority(int priority) {
			this.priority = priority;
		}
		
		public int getPriority() {
			return priority;
		}
		
		public static List<Priority> getOrderedPriorities() {
			return Arrays.asList(values()).stream().sorted((p1, p2) -> p2.getPriority() - p1.getPriority()).collect(Collectors.toList());
		}
	}
}
