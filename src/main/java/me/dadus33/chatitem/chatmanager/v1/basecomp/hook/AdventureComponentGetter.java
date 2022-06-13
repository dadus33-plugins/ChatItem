package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.PacketUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;

public class AdventureComponentGetter implements IBaseComponentGetter {

	@Override
	public boolean hasConditions() {
		try {
			for (String cl : Arrays.asList("net.kyori.adventure.text.Component",
					"net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer"))
				Class.forName(cl);
		} catch (ClassNotFoundException e) { // can't support this, adventure comp not found
			return false;
		}
		try {
			PacketUtils.getNmsClass("PacketPlayOutChat", "network.protocol.game.").getField("adventure$message");
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Component comp = packet.getContent().getSpecificModifier(Component.class).readSafely(0);
		if(comp == null)
			return null;
		String json = ComponentSerializer.toString(BungeeComponentSerializer.legacy().serialize(comp).clone());
		JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
		JsonObject next = new JsonObject();
		next.add("extra", jsonObj.get("with"));
		return next.toString();
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		JsonObject next = new JsonObject();
		next.addProperty("translate", "chat.type.text");
		next.add("with", fixParsedArray(JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("extra")));
		ChatItem.debug("Adventure Json: " + next.toString());
		packet.getContent().getSpecificModifier(Component.class).write(0, BungeeComponentSerializer.legacy().deserialize(ComponentSerializer.parse(next.toString())));
	}
	
	private JsonArray fixParsedArray(JsonArray arr) {
		JsonArray next = new JsonArray();
		for(JsonElement e : arr) {
			if(e.isJsonArray()) {
				JsonObject obj = null;
				JsonArray extra = new JsonArray();
				for(JsonElement element : e.getAsJsonArray()) {
					JsonElement fixed = checkElement(element);
					if(obj == null && fixed.isJsonObject()) {
						obj = fixed.getAsJsonObject();
					} else {
						extra.add(fixed);
					}
				}
				if(obj != null && extra.size() > 0) {
					obj.add("extra", extra);
				}
				next.add(obj);
			} else
				next.add(checkElement(e));
		}
		return next;
	}
	
	private JsonElement checkElement(JsonElement e) {
		if(e.isJsonObject()) {
			JsonObject o = e.getAsJsonObject();
			if(o.has("hoverEvent")) {// only hover event
				JsonObject hoverObject = o.getAsJsonObject("hoverEvent");
				if(hoverObject.get("action").getAsString().equalsIgnoreCase("show_item") && hoverObject.has("value")) {// if it's item to fix
					String val = hoverObject.get("value").getAsString();
					// now, we should fix the value
					val = val.replaceAll("\\\"", "\"");
					ChatItem.debug("Fixed json: " + ("{" + val.substring(1, val.length() - 1) + "}"));
					hoverObject.remove("value");
					hoverObject.add("contents", JsonParser.parseString("{" + val.substring(1, val.length() - 1) + "}"));
					o.add("hoverEvent", hoverObject);
				}
			}
		}
		return e;
	}
}
