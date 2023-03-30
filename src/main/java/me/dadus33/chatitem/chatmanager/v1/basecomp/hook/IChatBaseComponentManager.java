package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.lang.reflect.Method;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.PacketUtils;

public class IChatBaseComponentManager implements IComponentManager {

	private Method serializerGetJson;

	public IChatBaseComponentManager() {
		try {
			Class<?> chatSerializer = PacketUtils.CHAT_SERIALIZER;
			//fromJson = chatSerializer.getMethod("a", String.class);
			for (Method m : chatSerializer.getDeclaredMethods()) {
				if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(PacketUtils.COMPONENT_CLASS) && m.getReturnType().equals(String.class)) {
					serializerGetJson = m;
					break;
				}
			}
			if (serializerGetJson == null)
				ChatItem.getInstance().getLogger().warning("Failed to find JSON serializer in class: " + chatSerializer.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasConditions() {
		return serializerGetJson != null;
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		try {
			Object obj = packet.getContent().getChatComponents().readSafely(0);
			return obj == null ? null : (String) serializerGetJson.invoke(null, obj);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		try {
			packet.setPacket(PacketEditingChatManager.createSystemChatPacket(json));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
