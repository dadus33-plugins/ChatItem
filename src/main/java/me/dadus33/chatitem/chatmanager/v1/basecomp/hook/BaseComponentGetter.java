package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.Version;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class BaseComponentGetter implements IBaseComponentGetter {

	@Override
	public boolean hasConditions() {
		return Version.getVersion().isNewerOrEquals(Version.V1_8);
	}
	
	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		BaseComponent[] comps = packet.getContent().getSpecificModifier(BaseComponent[].class).readSafely(0);
		return comps == null ? null : ComponentSerializer.toString(comps);
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		packet.getContent().getSpecificModifier(BaseComponent[].class).write(0, ComponentSerializer.parse(json));
	}
}
