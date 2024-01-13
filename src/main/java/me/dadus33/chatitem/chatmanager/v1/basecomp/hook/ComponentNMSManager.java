package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.PacketUtils;

public class ComponentNMSManager implements IComponentManager {

	@Override
	public boolean hasConditions() {
		return PacketUtils.COMPONENT_CLASS != null;
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Object chatBaseComp = packet.getContent().getChatComponents().readSafely(0);
		if (chatBaseComp != null) {
			try {
				return PacketUtils.CHAT_SERIALIZER.getMethod("a", PacketUtils.COMPONENT_CLASS).invoke(null, chatBaseComp).toString();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		// Not compatible with that for now
		//if(ChatItem.discordSrvSupport && DiscordSrvSupport.isSendingMessage())
		//	DiscordSrvSupport.sendChatMessage(p, comp, null);
		try {
			packet.setPacket(PacketEditingChatManager.createSystemChatPacket(json));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
