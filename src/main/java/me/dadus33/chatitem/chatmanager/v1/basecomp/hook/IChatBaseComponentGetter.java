package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.lang.reflect.Method;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.PacketUtils;

public class IChatBaseComponentGetter implements IBaseComponentGetter {

	private Method serializerGetJson;
	
	public IChatBaseComponentGetter() {
		try {
			for (Method m : PacketUtils.CHAT_SERIALIZER.getDeclaredMethods()) {
				if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(PacketUtils.COMPONENT_CLASS)
						&& m.getReturnType().equals(String.class)) {
					serializerGetJson = m;
					break;
				}
			}
			if (serializerGetJson == null)
				ChatItem.getInstance().getLogger().warning(
						"Failed to find JSON serializer in class: " + PacketUtils.CHAT_SERIALIZER.getCanonicalName());
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
			packet.getContent().getChatComponents().write(0, PacketUtils.CHAT_SERIALIZER.getMethod("a", String.class).invoke(null, json));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
