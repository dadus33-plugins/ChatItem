package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.PacketUtils;

public class ComponentGetter implements IBaseComponentGetter {

	@Override
	public boolean hasConditions() {
		return PacketUtils.COMPONENT_CLASS != null;
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Object chatBaseComp = packet.getContent().getSpecificModifier(PacketUtils.COMPONENT_CLASS).readSafely(0);
		if (chatBaseComp != null) {
			try {
				return PacketUtils.CHAT_SERIALIZER.getMethod("a", PacketUtils.COMPONENT_CLASS)
						.invoke(null, packet.getPacket()).toString();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		return null;
	}

}
