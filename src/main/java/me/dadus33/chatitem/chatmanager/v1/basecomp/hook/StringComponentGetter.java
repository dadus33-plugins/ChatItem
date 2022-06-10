package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.PacketUtils;

public class StringComponentGetter implements IBaseComponentGetter{

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		return packet.getContent().getStrings().read(0);
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		try {
			Class<?> packetClass = PacketUtils.getNmsClass("ClientboundSystemChatPacket", "network.protocol.game.");
			packet.setPacket(packetClass.getConstructor(String.class, int.class).newInstance(json, 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
