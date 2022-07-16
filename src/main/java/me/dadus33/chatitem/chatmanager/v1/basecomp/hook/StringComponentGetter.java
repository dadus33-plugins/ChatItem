package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;

public class StringComponentGetter implements IBaseComponentGetter{

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		String json = packet.getContent().getStrings().readSafely(0);
		if(json != null && json.startsWith("[") && json.endsWith("]")) { // if used as array instead of json obj
			JsonArray extra = new JsonArray();
			for(JsonElement element : JsonParser.parseString(json).getAsJsonArray()) {
				if(element.isJsonObject()) { // ignore this
					extra.add(element);
				} else {
					ChatItem.debug("Ignoring element " + element);
				}
			}
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("text", "");
			jsonObject.add("extra", extra);
			json = jsonObject.toString();
		}
		return json;
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
