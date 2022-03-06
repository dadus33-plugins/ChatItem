package me.dadus33.chatitem.chatmanager.v1.utils;

import static me.dadus33.chatitem.utils.Version.V1_10;
import static me.dadus33.chatitem.utils.Version.V1_11;
import static me.dadus33.chatitem.utils.Version.V1_8;
import static me.dadus33.chatitem.utils.Version.V1_9;

import org.bukkit.inventory.ItemStack;

@Deprecated
public class ItemRewriter {

	public static String remapIds(int server, int player, ItemStack is) {
        Item item = new Item();
        item.setAmount((byte)is.getAmount());
        //item.setData(is.getDurability());
        //item.setId(id);
        //item.setTag(tag);
		if (areIdsCompatible(server, player)) {
			return item.getId();
		}
		if ((server >= V1_9.MIN_VER && player <= V1_8.MAX_VER) || (player >= V1_9.MIN_VER && server <= V1_8.MAX_VER)) {
			if ((server >= V1_9.MIN_VER && player <= V1_8.MAX_VER)) {
				ItemRewriter_1_9_TO_1_8.reversedToClient(item);
			} else
				ItemRewriter_1_9_TO_1_8.toClient(item);
		}
		if ((server <= V1_10.MAX_VER && player >= V1_11.MIN_VER)
				|| (player <= V1_10.MAX_VER && server >= V1_11.MIN_VER)) {
			if (server <= V1_10.MAX_VER && player >= V1_11.MIN_VER) {
				ItemRewriter_1_11_TO_1_10.toClient(item);
			} else {
				ItemRewriter_1_11_TO_1_10.reverseToClient(item);
			}
		}
		return item.getId();
	}

	public static boolean areIdsCompatible(int version1, int version2) {
		if ((version1 >= V1_9.MIN_VER && version2 <= V1_8.MAX_VER)
				|| (version2 >= V1_9.MIN_VER && version1 <= V1_8.MAX_VER)) {
			return false;
		}
		if ((version1 <= V1_10.MAX_VER && version2 >= V1_11.MIN_VER)
				|| (version1 <= V1_10.MAX_VER && version2 >= V1_11.MIN_VER)) {
			return false;
		}
		return true;
	}
}
