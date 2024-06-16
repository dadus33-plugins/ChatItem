package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.hook.DiscordSrvSupport;

public class ComponentNMSManager implements IComponentManager {

	@Override
	public boolean hasConditions() {
		return ChatItem.getPlatform().hasBaseComponentSerializer();
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Object chatBaseComp = packet.getContent().getChatComponents().readSafely(0);
		if (chatBaseComp != null) {
			try {
				Object o = ChatItem.getPlatform().baseComponentToJson(chatBaseComp);
				if(o != null && o instanceof String && JsonParser.parseString((String) o).isJsonObject()) {
					return (String) o;
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		// Not compatible with that for now
		if(ChatItem.discordSrvSupport && DiscordSrvSupport.isSendingMessage())
			ChatItem.debug("(v1 ComponentNMS) Can't send message to discord");
		//	DiscordSrvSupport.sendChatMessage(p, comp, null);
		try {
			packet.setPacket(PacketEditingChatManager.createSystemChatPacket(json, packet.getPacket()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
