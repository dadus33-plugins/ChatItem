package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import net.kyori.adventure.text.Component;

public class StringComponentGetter implements IBaseComponentGetter{

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		return packet.getContent().getStrings().readSafely(0);
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		try {
			packet.setPacket(packet.getPacket().getClass().getConstructor(Component.class, String.class, int.class).newInstance(null, json, 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
