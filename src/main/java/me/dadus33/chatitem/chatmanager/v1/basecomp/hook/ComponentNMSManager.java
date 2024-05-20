package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.lang.reflect.Field;

import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.hook.DiscordSrvSupport;
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
				Object o = PacketUtils.CHAT_SERIALIZER.getMethod("a", PacketUtils.COMPONENT_CLASS).invoke(null, chatBaseComp);
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
			Object obj = PacketEditingChatManager.createSystemChatPacket(json, packet.getPacket());
			ChatItem.debug("Created packet: " + obj.getClass().getPackage().getName() + "." + obj.getClass().getSimpleName());
			for(Field f : obj.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				ChatItem.debug("  > " + f.getName() + " (" + f.getType().getSimpleName() + "): " + f.get(obj));
			}
			packet.setPacket(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
