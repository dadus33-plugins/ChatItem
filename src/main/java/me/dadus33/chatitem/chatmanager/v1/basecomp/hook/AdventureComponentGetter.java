package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
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
			for (String cl : Arrays.asList("net.kyori.adventure.text.Component", "net.kyori.adventure.builder.AbstractBuilder",
					"net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer"))
				Class.forName(cl);
		} catch (Exception e) { // can't support this, adventure comp not found
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
		String json = ComponentSerializer.toString(BungeeComponentSerializer.get().serialize(comp).clone());
		JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
		ChatItem.debug("AdventureJSON : " + json + ", use extra: " + (!jsonObj.has("with")));
		if(jsonObj.has("with")) {
			JsonObject next = new JsonObject();
			next.add("extra", jsonObj.get("with"));
			return next.toString();
		}
		useExtra = true;
		return json;
	}
	
	@Override
	public String removePlaceholdersAndName(String json, String toReplace, Player foundedPlayer) {
		String tmpName = IBaseComponentGetter.super.removePlaceholdersAndName(json, toReplace, foundedPlayer);
		if(!tmpName.equals(json)) // if not the same -> name found and removed
			return tmpName;
		JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
		if(!jsonObj.has("extra"))
			return json;
		String searched = toReplace + foundedPlayer.getName();
		for(JsonElement element : jsonObj.get("extra").getAsJsonArray()) {
			if(element.isJsonObject()) {
				JsonObject withObj = element.getAsJsonObject();
				if(withObj.has("extra")) {
					String text = "";
					int possibleId = -1;
					JsonArray extraArray = withObj.get("extra").getAsJsonArray();
					for(int i = 0; i < extraArray.size(); i++) {
						JsonElement extra = extraArray.get(i);
						if(!extra.isJsonObject())
							continue; // prevent error but should not appear
						JsonObject extraObj = extra.getAsJsonObject();
						if(extraObj.has("text") && extraObj.get("text").isJsonPrimitive()) {
							String extraTxt = extraObj.get("text").getAsString();
							if(possibleId == -1) { // don't found the begin yet
								if(searched.startsWith(extraTxt)) { // begin of replaced
									possibleId = i;
									text = extraTxt;
								}
							} else {
								if(searched.startsWith(extraTxt, text.length()) || ChatManager.equalsSeparator(extraTxt)) {
									text += extraTxt;
									if(searched.equals(text)) { // found everything
										// change actual, then remove all old
										extraArray.set(i, JsonParser.parseString("{\"text\":\"" + Character.toString(ChatManager.SEPARATOR) + "\"}"));
										for(int x = possibleId; x < i; x++)
											extraArray.remove(possibleId); // remove elements
										ChatItem.debug("Cleaned: " + jsonObj.toString());
										return jsonObj.toString();
									}
								} else {
									possibleId = -1; // was not good things
									text = "";
								}
							}
						}
					}
					ChatItem.debug("Failed to find: " + text + ", id: " + possibleId + " for " + withObj);
				}
			} // ignoring all others because it should not appear
		}
		ChatItem.debug("Nothing founded while trying to remove placeholders");
		return IBaseComponentGetter.super.removePlaceholdersAndName(json, toReplace, foundedPlayer);
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
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
		ChatItem.debug("Adventure Json: " + localJson);
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
				packet.setPacket(PacketEditingChatManager.createSystemChatPacket(localJson));
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
