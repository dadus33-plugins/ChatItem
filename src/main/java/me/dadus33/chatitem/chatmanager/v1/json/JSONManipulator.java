package me.dadus33.chatitem.chatmanager.v1.json;

import static me.dadus33.chatitem.utils.PacketUtils.getNmsClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ReflectionUtils;

public class JSONManipulator {

	private static JSONManipulator instance = new JSONManipulator();

	public static JSONManipulator getInstance() {
		return instance;
	}

	public static final Class<?> CRAFT_ITEM_STACK_CLASS = PacketUtils.getObcClass("inventory.CraftItemStack");
	public static final Class<?> NMS_ITEM_STACK_CLASS = getNmsClass("ItemStack", "world.item.");
	public static final Method AS_NMS_COPY = ReflectionUtils.getMethod(CRAFT_ITEM_STACK_CLASS, "asNMSCopy", ItemStack.class);
	public static final Class<?> NBT_TAG_COMPOUND = getNmsClass("NBTTagCompound", "nbt.", "CompoundTag");
	public static final Method SAVE_NMS_ITEM_STACK_METHOD = ReflectionUtils.getMethod(NMS_ITEM_STACK_CLASS, NBT_TAG_COMPOUND, NBT_TAG_COMPOUND);
	public static final Field MAP = ReflectionUtils.getField(NBT_TAG_COMPOUND, "map", "x");

	private JsonArray classicTooltip;
	
	public static JsonObject parseOrGet(String json) {
		if(json.startsWith("\"") && json.endsWith("\"")) {// seems to be simple line
			return JsonParser.parseString("{\"text\":" + json + "}").getAsJsonObject();
		}
		try {
			return JsonParser.parseString(json).getAsJsonObject();
		} catch (Exception e) {
			JsonObject obj = new JsonObject();
			obj.addProperty("text", json);
			return obj;
		}
	}

	public String parse(Chat chat, String json, ChatAction action, String replacement) throws Exception {
		JsonObject obj = parseOrGet(json);

		JsonObject wrapper = new JsonObject(); // Create a wrapper object for the whole array
		JsonArray use = Translator.toJson(replacement); // We get the json representation of the old color
														// formatting method
		ChatItem.debug("Remplacement: " + replacement + " use: " + use.toString());
		// There's no public clone method for JSONObjects so we need to parse them every
		// time
		JsonObject hover = new JsonObject();
		if (action.isItem()) {
			hover.addProperty("action", "show_item");

			// Get the JSON representation of the item (well, not really JSON, but rather a
			// string representation of NBT data)
			String item = ChatItem.getPlatform().stringifyItem(action.getItem());
			hover.addProperty("value", item);
			hover.add("contents", parseOrGet(item));
		} else {
			hover.addProperty("action", "show_text");

			// Get the JSON representation of the item (well, not really JSON, but rather a
			// string representation of NBT data)
			JsonArray hoverArray = new JsonArray();
			hoverArray.add(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", chat.getItemPlayer().getPlayer().getName()));
			hover.add("value", hoverArray);

			JsonObject click = new JsonObject();
			click.addProperty("action", "run_command");
			click.addProperty("value", action.getCommand());
			wrapper.add("clickEvent", click);
		}

		if (use.size() == 1) {
			JsonElement extraElement = use.get(0);
			if (extraElement.isJsonPrimitive())
				wrapper.addProperty("text", extraElement.getAsString());
			else if (extraElement.isJsonObject())
				wrapper.addProperty("text", extraElement.getAsJsonObject().get("text").getAsString());
			else
				wrapper.add("extra", use); // add it only if
		} else if (!use.isEmpty())
			wrapper.add("extra", use); // add it only if
		if (!wrapper.has("text"))
			wrapper.addProperty("text", ""); // The text field is compulsory, even if it's empty
		ChatItem.debug("Wrapper " + wrapper + " > " + use);
		wrapper.add("hoverEvent", hover);

		if (obj.size() == 1 && obj.has("text")) {
			wrapper.add("text", obj.get("text"));
			ChatItem.debug("[JsonManipulator] Parsed quick: " + obj.toString() + ", wrapper: " + wrapper);
			return ChatManager.replaceSeparator(chat, wrapper.toString(), replacement);
		}
		obj.add("extra", parseArray(obj.has("extra") ? obj.getAsJsonArray("extra") : new JsonArray(), wrapper));
		if (!obj.has("text")) {
			obj.addProperty("text", "");
		}
		return obj.toString();
	}

	@SuppressWarnings("deprecation")
	public String parseEmpty(Chat chat, String json, List<String> tooltip, Player sender) {
		JsonObject obj = parseOrGet(json);
		JsonArray array = obj.has("extra") ? obj.getAsJsonArray("extra") : new JsonArray();
		JsonArray use = Translator.toJson(ChatManager.getNameForChatAction(chat.getPlayer(), chat, ChatItem.getInstance().getStorage()).replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName()));
		JsonObject hover = JsonParser.parseString("{\"action\":\"show_text\", \"value\": \"\"}").getAsJsonObject();

		StringBuilder oneLineTooltip = new StringBuilder("");
		int index = 0;
		for (String m : tooltip) {
			oneLineTooltip.append(m.replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName()));
			++index;
			if (index != tooltip.size()) {
				oneLineTooltip.append('\n');
			}
		}

		hover.add("value", new JsonPrimitive(oneLineTooltip.toString()));
		if (!tooltip.isEmpty()) {
			for (JsonElement ob : use)
				ob.getAsJsonObject().add("hoverEvent", hover);
			classicTooltip = use;
		}
		obj.add("extra", parseArray(array, classicTooltip));
		if (!obj.has("text")) {
			obj.addProperty("text", "");
		}
		return obj.toString();
	}

	private JsonArray parseArray(JsonArray arr, JsonElement tooltip) {
		JsonArray replacer = new JsonArray();
		boolean separator = false;
		for (int i = 0; i < arr.size(); ++i) {
			JsonElement element = arr.get(i);
			if(element.isJsonNull())
				continue;
			if(separator) {
				if(ChatManager.containsSeparatorEnd(element.toString()))
					separator = false;
				continue;
			}
			if (element.isJsonObject()) {
				JsonElement text = element.getAsJsonObject().get("text");
				if(text != null && ChatManager.containsSeparator(text.getAsString())) {
					if(!ChatManager.containsSeparatorEnd(text.getAsString())) // if the separator doesn't end in the same string as it's begin
						separator = true;
				}
				ChatItem.debug("Parsing object " + element.toString());
				addParsedJsonObjectToArray(element.getAsJsonObject(), replacer, tooltip);
			} else if (element.isJsonArray()) {
				JsonArray jar = element.getAsJsonArray();
				if (jar.size() != 0) {
					jar = parseArray(element.getAsJsonArray(), tooltip);
					replacer.set(i, jar);
				}
			} else if(element.isJsonPrimitive()) {
				if(ChatManager.containsSeparator(element.getAsString())) {
					if(!ChatManager.containsSeparatorEnd(element.getAsString())) // if the separator doesn't end in the same string as it's begin
						separator = true;
					addParsedStringToArray(element.getAsString(), replacer, element, tooltip);
				}
			}
		}
		return replacer;
	}

	private void addParsedJsonObjectToArray(JsonObject o, JsonArray rep, JsonElement tooltip) {
		JsonElement text = o.get("text");
		if (text == null) {
			JsonElement el = o.get("extra");
			if (el != null) {
				JsonArray jar = el.getAsJsonArray();
				if (jar.size() != 0) {
					JsonArray tmpArray = parseArray(jar, tooltip);
					if (!tmpArray.isEmpty())
						o.add("extra", tmpArray);
					else
						o.remove("extra");
				} else {
					o.remove("extra");
				}
			}
			return;
		} else {
			if (text.getAsString().isEmpty()) {
				JsonElement el = o.get("extra");
				if (el != null) {
					JsonArray jar = el.getAsJsonArray();
					if (!jar.isEmpty()) {
						JsonArray tmpArray = parseArray(jar, tooltip);
						if (!tmpArray.isEmpty())
							o.add("extra", tmpArray);
						else
							o.remove("extra");
					} else {
						o.remove("extra");
					}
				}
			}
		}

		addParsedStringToArray(text.getAsString(), rep, o, tooltip);
	}

	private void addParsedStringToArray(String msg, JsonArray rep, JsonElement o, JsonElement tooltip) {
		if (!ChatManager.containsSeparator(msg)) {
			rep.add(o);
			return;
		}
		ChatItem.debug("[JSONManipulator] Parsed string " + msg + ", rep: " + rep);
		String current = "";
		boolean wasSep = false;
		for (String parts : msg.split("")) {
			if (ChatManager.equalsSeparator(parts)) {
				if (!current.isEmpty()) {
					if(o.isJsonObject()) {
						JsonObject jsonObj = o.getAsJsonObject().deepCopy();
						jsonObj.addProperty("text", current); // edit text
						rep.add(jsonObj); // add with all basic coloring things
					} else {
						rep.add(current);
					}
					current = "";
				}
				rep.add(tooltip);
				wasSep = true;
			} else if (wasSep) {
				if (ChatManager.equalsSeparatorEnd(parts)) // sep finished
					wasSep = false;
			} else {
				current += parts;
			}
		}
		if (!current.isEmpty()) {
			if(o.isJsonObject()) {
				JsonObject jsonObj = o.getAsJsonObject().deepCopy();
				jsonObj.addProperty("text", current); // edit text
				rep.add(jsonObj); // add with all basic coloring things
			} else {
				rep.add(current);
			}
		}
	}
}
