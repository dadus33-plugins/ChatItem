package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent.ContentModifier;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ReflectionUtils;

public class PCMComponentManager implements IComponentManager {

	@Override
	public boolean hasConditions() {
		try {
			for (String cl : Arrays.asList("net.minecraft.network.chat.PlayerChatMessage"))
				Class.forName(cl);
		} catch (Exception e) { // can't support this
			ChatItem.debug("Can't load PCMComponentManager : " + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Class<?> pcmClass = PacketUtils.getNmsClass("PlayerChatMessage", "network.chat.");
		Object pcm = packet.getContent().getSpecificModifier(pcmClass).readSafely(0);
		if (pcm != null) {
			try {
				Object chatBaseComp = ReflectionUtils.getMethod(pcmClass, PacketUtils.COMPONENT_CLASS).invoke(pcm);
				if(chatBaseComp != null)
					ChatItem.debug("[PCMManager] Founded " + chatBaseComp.getClass().getSimpleName());
				return chatBaseComp == null ? null : PacketUtils.CHAT_SERIALIZER.getMethod("a", PacketUtils.COMPONENT_CLASS).invoke(null, chatBaseComp).toString();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		try {
			Object chatComp = PacketUtils.CHAT_SERIALIZER.getMethod("a", String.class).invoke(null, json);
			Class<?> pcmClass = PacketUtils.getNmsClass("PlayerChatMessage", "network.chat.");
			ContentModifier<Object> pcmModifier = packet.getContent().getSpecificModifier((Class<Object>) pcmClass);
			Object pcm = ReflectionUtils.getMethod(pcmClass, pcmClass, PacketUtils.COMPONENT_CLASS).invoke(pcmModifier.readSafely(0), chatComp);
			for(Constructor<?> cons : packet.getPacket().getClass().getConstructors()) {
				if(cons.getParameterCount() <= 1)
					continue;
				Object[] params = new Object[cons.getParameterCount()];
				for(int i = 0; i < params.length; i++) {
					Class<?> cls = cons.getParameterTypes()[i];
					if(cls.equals(pcmClass)) {
						params[i] = pcm;
					} else
						params[i] = packet.getContent().getSpecificModifier(cls).readSafely(0);
				}
				packet.setPacket(cons.newInstance(params));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
