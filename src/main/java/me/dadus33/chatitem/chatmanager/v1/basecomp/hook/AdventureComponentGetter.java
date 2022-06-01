package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.util.Arrays;

import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;

public class AdventureComponentGetter implements IBaseComponentGetter {

	@Override
	public boolean hasConditions() {
		try {
			for (String cl : Arrays.asList("net.kyori.adventure.text.Component",
					"net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer"))
				Class.forName(cl);
		} catch (ClassNotFoundException e) { // can't support this, adventure comp not found
			return false;
		}
		try {
			PacketUtils.getNmsClass("PacketPlayOutChat", "network.protocol.game.").getField("adventure$message");
		} catch (NoSuchFieldException e) {
			return false;
		}
		return true;
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Component comp = packet.getContent().getSpecificModifier(Component.class).readSafely(0);
		return comp == null ? null : ComponentSerializer.toString(BungeeComponentSerializer.legacy().serialize(comp));
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		packet.getContent().getSpecificModifier(Component.class).write(0, BungeeComponentSerializer.get().deserialize(ComponentSerializer.parse(json)));
	}
}
