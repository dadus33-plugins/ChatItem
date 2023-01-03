package me.dadus33.chatitem.chatmanager.v1.json;

import static me.dadus33.chatitem.utils.PacketUtils.getNmsClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Reflect;
import me.dadus33.chatitem.utils.Version;

public class JSONManipulator {
	
	private static JSONManipulator instance = new JSONManipulator();
	
	public static JSONManipulator getInstance() {
		return instance;
	}
	private static final List<String> SKIPPED = Arrays.asList("HSTRY_ENCHANTS");

	public static final Class<?> CRAFT_ITEM_STACK_CLASS = PacketUtils.getObcClass("inventory.CraftItemStack");
	public static final Class<?> NMS_ITEM_STACK_CLASS = getNmsClass("ItemStack", "world.item.");
	public static final Method AS_NMS_COPY = Reflect.getMethod(CRAFT_ITEM_STACK_CLASS, "asNMSCopy", ItemStack.class);
	public static final Class<?> NBT_TAG_COMPOUND = getNmsClass("NBTTagCompound", "nbt.");
	public static final Method SAVE_NMS_ITEM_STACK_METHOD = Reflect.getMethod(NMS_ITEM_STACK_CLASS, NBT_TAG_COMPOUND,
			NBT_TAG_COMPOUND);
	public static final Field MAP = Reflect.getField(NBT_TAG_COMPOUND, "map", "x");

	private static final ConcurrentHashMap<Map.Entry<Version, ItemStack>, JsonObject> STACKS = new ConcurrentHashMap<>();

	private JsonObject itemTooltip;
	private JsonArray classicTooltip;

	public String parse(String json, ItemStack item, String replacement, int protocol)
			throws Exception {
		JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
		final AbstractMap.SimpleEntry<Version, ItemStack> p = new AbstractMap.SimpleEntry<>(Version.getVersion(protocol), item);

		JsonObject wrapper = new JsonObject(); // Create a wrapper object for the whole array
		if ((itemTooltip = STACKS.get(p)) == null) {
			JsonArray use = Translator.toJson(replacement); // We get the json representation of the old color
															// formatting method
			ChatItem.debug("Remplacement: " + replacement + " use: " + use.toString());
			// There's no public clone method for JSONObjects so we need to parse them every time
			JsonObject hover = new JsonObject();
			hover.addProperty("action", "show_item");

			// Get the JSON representation of the item (well, not really JSON, but rather a string representation of NBT data)
			hover.addProperty("value", stringifyItem(item));

			if(use.size() == 1) {
				JsonElement extraElement = use.get(0);
				if(extraElement.isJsonPrimitive())
					wrapper.addProperty("text", extraElement.getAsString());
				else if(extraElement.isJsonObject())
					wrapper.addProperty("text", extraElement.getAsJsonObject().get("text").getAsString());
				else
					wrapper.add("extra", use); // add it only if
			} else if(!use.isEmpty())
				wrapper.add("extra", use); // add it only if
			if(!wrapper.has("text"))
				wrapper.addProperty("text", ""); // The text field is compulsory, even if it's empty
			wrapper.add("hoverEvent", hover);

			itemTooltip = wrapper; // Save the tooltip for later use when we encounter a placeholder
			STACKS.put(p, itemTooltip); // Save it in the cache too so when parsing other packets with the same item
										// (and client version) we no longer have to create it again
			// We remove it later when no longer needed to save memory
			Bukkit.getScheduler().runTaskLaterAsynchronously(ChatItem.getInstance(), () -> STACKS.remove(p), 100L);
		}
		if(obj.size() == 1 && obj.has("text")) {
			itemTooltip.add("text", obj.get("text"));
			ChatItem.debug("[JsonManipulator] Parsed quick: " + obj.toString() + ", wrapper: " + itemTooltip);
			return itemTooltip.toString().replace(Character.toString(ChatManager.SEPARATOR), "").replace(ChatManager.SEPARATOR_STR, "");
		}
		obj.add("extra", parseArray(obj.has("extra") ? obj.getAsJsonArray("extra") : new JsonArray(), itemTooltip));
		if (!obj.has("text")) {
			obj.addProperty("text", "");
		}
		ChatItem.debug("Parsed array for item: " + obj.toString() + ", wrapper: " + itemTooltip);
		return obj.toString();
	}

	public String parseEmpty(String json, String repl, List<String> tooltip, Player sender) {
		JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
		JsonArray array = obj.has("extra") ? obj.getAsJsonArray("extra") : new JsonArray();
		JsonArray use = Translator
				.toJson(repl.replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName()));
		JsonObject hover = JsonParser.parseString("{\"action\":\"show_text\", \"value\": \"\"}").getAsJsonObject();

		StringBuilder oneLineTooltip = new StringBuilder("");
		int index = 0;
		for (String m : tooltip) {
			oneLineTooltip
					.append(m.replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName()));
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
		boolean isComplex = false;
		for (int i = 0; i < arr.size(); ++i) {
			JsonElement element = arr.get(i);
			if (element.isJsonNull()) {
				continue;
			} else if (element.isJsonObject()) {
				isComplex = true;
				addParsedJsonObjectToArray(element.getAsJsonObject(), replacer, tooltip);
			} else if (element.isJsonArray()) {
				isComplex = true;
				JsonArray jar = element.getAsJsonArray();
				if (jar.size() != 0) {
					jar = parseArray(element.getAsJsonArray(), tooltip);
					replacer.set(i, jar);
				}
			} else if(element.isJsonPrimitive() && isComplex) {
				ChatItem.debug("[JSONManipulator] Put " + element + " to trash as it's primitive with complex things");
				// ignore
			} else {
				addParsedStringToArray(element.getAsString(), replacer, element, tooltip);
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
					if(!tmpArray.isEmpty())
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
						if(!tmpArray.isEmpty())
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
		ChatItem.debug("Checking " + msg + " > " + rep + ", o: " + o);
		if(!ChatManager.containsSeparator(msg)) {
			rep.add(o);
			return;
		}
		String current = "";
		boolean wasSep = false;
		for(String parts : msg.split("")) {
			if(ChatManager.equalsSeparator(parts)) {
				if(!current.isEmpty()) {
					rep.add(current);
					current = "";
				}
				rep.add(tooltip);
				wasSep = true;
			} else if(wasSep) {
				if(parts == " ") { // sep finished
					wasSep = false;
					current += parts;
				}
			} else {
				current += parts;
			}
		}
		if(!current.isEmpty())
			rep.add(current);
	}

	public static String stringifyItem(ItemStack is) {
		try {
			return stringifyItemInternal(is);
		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}
	}

	@SuppressWarnings({ "deprecation" })
	public static String stringifyItemInternal(ItemStack is) throws Exception {
		Object nmsStack = JSONManipulator.AS_NMS_COPY.invoke(null, is);
		Object nmsTag = JSONManipulator.NBT_TAG_COMPOUND.newInstance();
		JSONManipulator.SAVE_NMS_ITEM_STACK_METHOD.invoke(nmsStack, nmsTag);
		HashMap<String, String> tagMap = new HashMap<>();
		Map<String, Object> nmsMap = (Map<String, Object>) JSONManipulator.MAP.get(nmsTag);
		String id = nmsMap.get("id").toString().replace("\"", "");
		Object realTag = nmsMap.get("tag");
		if (JSONManipulator.NBT_TAG_COMPOUND.isInstance(realTag)) { // We need to make sure this is indeed an
																			// NBTTagCompound
			Map<String, Object> realMap = (Map<String, Object>) JSONManipulator.MAP.get(realTag);
			Set<Map.Entry<String, Object>> entrySet = realMap.entrySet();
			for (Map.Entry<String, Object> entry : entrySet) {
				tagMap.put(entry.getKey(), entry.getValue().toString());
			}
		}
		// TODO check for ID remapping
		// ItemRewriter.remapIds(Version.getVersion().MAX_VER, protocolVersion.MAX_VER,
		// is);
		StringBuilder sb = new StringBuilder("{id:");
		sb.append("\"").append(id).append("\"").append(","); // Append the id
		sb.append("Count:").append(is.getAmount()).append("b"); // Append the amount

		if (!tagMap.containsKey("Damage")) { // for new versions
			sb.append(",Damage:").append(is.getDurability()).append("s"); // Append the durability data
		}
		if (tagMap.isEmpty()) {
			sb.append("}");
			return sb.toString();
		}
		Set<Map.Entry<String, String>> entrySet = tagMap.entrySet();
		boolean first = true;
		sb.append(",tag:{"); // Start of the tag
		for (Map.Entry<String, String> entry : entrySet) {
			if(SKIPPED.contains(entry.getKey()))
				continue;
			if (!first)
				sb.append(",");
			first = false;
			if (!entry.getKey().isEmpty()) {
				if(entry.getKey().contains(";"))
					sb.append("\"" + entry.getKey() + "\"");
				else
					sb.append(entry.getKey());
				sb.append(":");
			}
			ChatItem.debug("Cleaning " + entry.getKey() + ": " + entry.getValue() + " > " + cleanStr(entry.getValue()));
			sb.append(cleanStr(entry.getValue()));
		}
		sb.append("}}"); // End of tag and end of item
		return sb.toString();
	}

	private static String cleanStr(String s) {
		return s.startsWith("'") && s.endsWith("'") ? s.substring(1, s.length() - 1) : s;
	}
}
