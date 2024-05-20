package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;

public class IChatBaseComponentManager implements IComponentManager {

	@Override
	public boolean hasConditions() {
		return ChatItem.getPlatform().hasBaseComponentSerializer();
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		try {
			Object obj = packet.getContent().getChatComponents().readSafely(0);
			return obj == null ? null : (String) ChatItem.getPlatform().baseComponentToJson(obj);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		try {
			packet.setPacket(PacketEditingChatManager.createSystemChatPacket(json, packet.getPacket()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
