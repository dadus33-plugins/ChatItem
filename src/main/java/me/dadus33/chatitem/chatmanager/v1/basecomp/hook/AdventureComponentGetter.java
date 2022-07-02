package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.ReflectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;

public class AdventureComponentGetter implements IBaseComponentGetter {

	private boolean useExtra = false;
	
	@Override
	public boolean hasConditions() {
		try {
			for (String cl : Arrays.asList("net.kyori.adventure.text.Component",
					"net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer"))
				Class.forName(cl);
		} catch (ClassNotFoundException e) { // can't support this, adventure comp not found
			return false;
		}
		return true;
	}
	
	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Component comp = packet.getContent().getSpecificModifier(Component.class).readSafely(0);
		if(comp == null) {
			ChatItem.debug("The component is null.");
			return null;
		}
		String json = ComponentSerializer.toString(BungeeComponentSerializer.legacy().serialize(comp).clone());
		ChatItem.debug("AdventureJSON : " + json.replace(ChatManager.SEPARATOR, 'S'));
		JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
		if(jsonObj.has("with")) {
			JsonObject next = new JsonObject();
			next.add("extra", jsonObj.get("with"));
			return next.toString();
		}
		useExtra = true;
		return json;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		/*JsonObject next = new JsonObject();
		next.addProperty("translate", "chat.type.text");
		if(useExtra)
			next.add("with", fixParsedArray(JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("extra")));
		else
			next.add("extra", fixParsedArray(JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("extra")));*/
		JsonElement globalElement = JsonParser.parseString(json);
		JsonArray extraArray = fixParsedArray(globalElement.getAsJsonObject().getAsJsonArray("extra"));
		String localJson;
		if(useExtra) {
			globalElement.getAsJsonObject().add("extra", extraArray);
			localJson = globalElement.toString();
		} else {
			JsonObject next = new JsonObject();
			next.addProperty("translate", "chat.type.text");
			next.add("with", extraArray);
			localJson = next.toString();
		}
		ChatItem.debug("Adventure Json: " + json);
		try {
			Class<?> packetClass = packet.getPacket().getClass();
			if(packetClass.getSimpleName().equalsIgnoreCase("PacketPlayOutChat")) {
				Field componentField = ReflectionUtils.getFirstFieldWith(packetClass, Component.class);
				if(componentField != null) {
					componentField.setAccessible(true);
					componentField.set(packet.getPacket(), BungeeComponentSerializer.legacy().deserialize(ComponentSerializer.parse(localJson)));
				} else {
					throw new UnsupportedOperationException("The packet PacketPlayOutChat doesn't have Kyori's field. It has: " + Arrays.asList(packetClass.getDeclaredFields()).stream().map(f -> f.getName() + ": " + f.getType().getName()).collect(Collectors.toList()));
				}
			} else if(packetClass.getSimpleName().equalsIgnoreCase("ClientboundSystemChatPacket")) {
				packet.setPacket(packetClass.getConstructor(Component.class, String.class, int.class).newInstance(BungeeComponentSerializer.legacy().deserialize(ComponentSerializer.parse(localJson)), null, 1));
			} else {
				throw new UnsupportedOperationException("The packet " + packetClass.getSimpleName() + " isn't supported by the AdventureGetter. Please report this.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JsonArray fixParsedArray(JsonArray arr) {
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
					JsonElement tagElement = JsonParser.parseString("{" + val.substring(1, val.length() - 1) + "}");
					tagElement.getAsJsonObject().remove("tag");
					/*if(tagElement.getAsJsonObject().has("tag")) {
						JsonElement tag = tagElement.getAsJsonObject().get("tag");
						if(tag.isJsonObject()) {
							JsonObject tagObj = tag.getAsJsonObject();
							tagObj.entrySet().forEach(entry -> {
								ChatItem.debug("Tag json: " + entry.getKey() + " > " + entry.getValue());
								JsonElement value = entry.getValue();
								String sval = value.getAsString();
								if(Utils.isInteger(sval)) {
									if(Utils.isByte(sval))
										tagObj.addProperty(entry.getKey(), sval + "b");
									else if(Utils.isShort(sval))
										tagObj.addProperty(entry.getKey(), sval + "s");
									else if(Utils.isLong(sval))
										tagObj.addProperty(entry.getKey(), sval + "l");
									else
										tagObj.addProperty(entry.getKey(), value + "i");
								}
							});
						}
					}*/
					hoverObject.add("contents", tagElement);
					o.add("hoverEvent", hoverObject);
				}
			}
		}
		return e;
	}
}
