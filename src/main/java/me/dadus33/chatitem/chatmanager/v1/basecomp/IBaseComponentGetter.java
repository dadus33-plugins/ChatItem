package me.dadus33.chatitem.chatmanager.v1.basecomp;

import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;

public interface IBaseComponentGetter {

	default boolean hasConditions() {
		return true;
	}
	
	String getBaseComponentAsJSON(ChatItemPacket packet);
	
}
