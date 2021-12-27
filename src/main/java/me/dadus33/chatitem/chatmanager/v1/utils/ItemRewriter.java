package me.dadus33.chatitem.chatmanager.v1.utils;

import static me.dadus33.chatitem.utils.ProtocolVersion.V1_10;
import static me.dadus33.chatitem.utils.ProtocolVersion.V1_11;
import static me.dadus33.chatitem.utils.ProtocolVersion.V1_8;
import static me.dadus33.chatitem.utils.ProtocolVersion.V1_9;

public class ItemRewriter {

	public static void remapIds(int server, int player, Item item) {
		if (areIdsCompatible(server, player)) {
			return;
		}
		if ((server >= V1_9.MIN_VER && player <= V1_8.MAX_VER) || (player >= V1_9.MIN_VER && server <= V1_8.MAX_VER)) {
			if ((server >= V1_9.MIN_VER && player <= V1_8.MAX_VER)) {
				ItemRewriter_1_9_TO_1_8.reversedToClient(item);
				return;
			}
			ItemRewriter_1_9_TO_1_8.toClient(item);
			return;
		}
		if ((server <= V1_10.MAX_VER && player >= V1_11.MIN_VER)
				|| (player <= V1_10.MAX_VER && server >= V1_11.MIN_VER)) {
			if (server <= V1_10.MAX_VER && player >= V1_11.MIN_VER) {
				ItemRewriter_1_11_TO_1_10.toClient(item);
			} else {
				ItemRewriter_1_11_TO_1_10.reverseToClient(item);
			}
		}
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
