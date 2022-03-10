package me.dadus33.chatitem.chatmanager.v1.json;

import static me.dadus33.chatitem.utils.PacketUtils.getNmsClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Reflect;
import me.dadus33.chatitem.utils.Version;

@SuppressWarnings({ "unchecked" })
public class JSONManipulatorCurrent {

	private static final Class<?> CRAFT_ITEM_STACK_CLASS = PacketUtils.getObcClass("inventory.CraftItemStack");
	private static final Class<?> NMS_ITEM_STACK_CLASS = getNmsClass("ItemStack", "world.item.");
	private static final Method AS_NMS_COPY = Reflect.getMethod(CRAFT_ITEM_STACK_CLASS, "asNMSCopy", ItemStack.class);
	private static final Class<?> NBT_TAG_COMPOUND = getNmsClass("NBTTagCompound", "nbt.");
	private static final Method SAVE_NMS_ITEM_STACK_METHOD = Reflect.getMethod(NMS_ITEM_STACK_CLASS, NBT_TAG_COMPOUND,
			NBT_TAG_COMPOUND);
	private static final Field MAP = Reflect.getField(NBT_TAG_COMPOUND, "map", "x");

	private static final ConcurrentHashMap<Map.Entry<Version, ItemStack>, JsonObject> STACKS = new ConcurrentHashMap<>();

	private List<String> replaces;
	private String rgx;
	private Version protocolVersion;
	private JsonObject itemTooltip;
	private JsonArray classicTooltip;

	public String parse(String json, List<String> replacements, ItemStack item, String replacement, int protocol)
			throws Exception {
		JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
		JsonArray array = obj.getAsJsonArray("extra");
		replaces = replacements;
		String regex = "";
		for (int i = 0; i < replacements.size(); ++i) {
			if (replacements.size() == 1) {
				regex = Pattern.quote(replacements.get(0));
				break;
			}
			if (i == 0 || i + 1 == replacements.size()) {
				if (i == 0) {
					regex = "(" + Pattern.quote(replacements.get(i));
				} else {
					regex = regex.concat("|").concat(Pattern.quote(replacements.get(i))).concat(")");
				}
				continue;
			}
			regex = regex.concat("|").concat(Pattern.quote(replacements.get(i)));
		}
		rgx = regex;
		JsonArray rep = new JsonArray();
		final AbstractMap.SimpleEntry<Version, ItemStack> p = new AbstractMap.SimpleEntry<>(
				protocolVersion = Version.getVersion(protocol), item);

		if ((itemTooltip = STACKS.get(p)) == null) {
			JsonArray use = Translator.toJson(replacement); // We get the json representation of the old color
															// formatting method
			// There's no public clone method for JSONObjects so we need to parse them every
			// time
			JsonObject hover = JsonParser.parseString("{\"action\":\"show_item\", \"value\": \"\"}").getAsJsonObject();

			String jsonRep = stringifyItem(this, item, protocolVersion);// stringifyItem(item); // Get the JSON
																		// representation of the item (well, not really
																		// JSON, but
			// rather a string representation of NBT data)
			hover.addProperty("value", jsonRep);

			JsonObject wrapper = new JsonObject(); // Create a wrapper object for the whole array
			wrapper.addProperty("text", ""); // The text field is compulsory, even if it's empty
			wrapper.add("extra", use);
			wrapper.add("hoverEvent", hover);

			itemTooltip = wrapper; // Save the tooltip for later use when we encounter a placeholder
			STACKS.put(p, itemTooltip); // Save it in the cache too so when parsing other packets with the same item
										// (and client version) we no longer have to create it again
			// We remove it later when no longer needed to save memory
			Bukkit.getScheduler().runTaskLaterAsynchronously(ChatItem.getInstance(), () -> STACKS.remove(p), 100L);
		}

		for (int i = 0; i < array.size(); ++i) {
			if (array.get(i).isJsonObject()) {
				JsonObject o = array.get(i).getAsJsonObject();
				boolean inside = false;
				for (String replace : replacements)
					if (o.toString().contains(replace)) {
						if (inside) {
							break;
						}
						inside = true;
					}
				JsonElement text = o.get("text");
				if (text == null) {
					JsonElement el = o.get("extra");
					if (el != null) {
						JsonArray jar = el.getAsJsonArray();
						if (jar.size() != 0) {
							o.add("extra", parseArray(jar));
						} else {
							o.remove("extra");
						}
					}
					continue;
				} else {
					if (text.getAsString().isEmpty()) {
						JsonElement el = o.get("extra");
						if (el != null) {
							JsonArray jar = el.getAsJsonArray();
							if (jar.size() != 0) {
								o.add("extra", parseArray(jar));
							} else {
								o.remove("extra");
							}
						}
					}
				}

				String msg = text.getAsString();
				boolean isLast = false;
				String[] splits;
				for (String repls : replacements) {
					isLast = msg.endsWith(repls);
					if (isLast) {
						msg = msg.concat(".");
						break;
					}
				}
				splits = msg.split(regex);
				if (splits.length != 1) {
					for (int j = 0; j < splits.length; ++j) {
						boolean endDot = (j == splits.length - 1) && isLast;
						if (!splits[j].isEmpty() && !endDot) {
							String st = o.toString();
							JsonObject fix = JsonParser.parseString(st).getAsJsonObject();
							fix.addProperty("text", splits[j]);
							rep.add(fix);
						}
						if (j != splits.length - 1) {
							rep.add(itemTooltip);
						}
					}
				} else {
					rep.add(o);
				}
			} else {
				if (array.get(i).isJsonNull()) {
					continue;
				} else {
					if (array.get(i).isJsonArray()) {
						JsonArray jar = array.get(i).getAsJsonArray();
						if (jar.size() != 0) {
							rep.set(i, parseArray(array.get(i).getAsJsonArray()));
						}
					} else {
						String msg = array.get(i).getAsString();
						boolean isLast = false;
						for (String repls : replacements) {
							isLast = msg.endsWith(repls);
							if (isLast) {
								msg = msg.concat(".");
								break;
							}
						}
						String[] splits = msg.split(regex);
						if (splits.length != 1) {
							for (int j = 0; j < splits.length; ++j) {
								boolean endDot = (j == splits.length - 1) && isLast;
								if (!splits[j].isEmpty() && !endDot) {
									rep.add(new JsonPrimitive(splits[j]));
								}
								if (j != splits.length - 1) {
									rep.add(itemTooltip);
								}
							}
						} else {
							rep.add(array.get(i));
						}
					}
				}
			}

		}
		obj.add("extra", rep);
		if (!obj.has("text")) {
			obj.addProperty("text", "");
		}
		return obj.toString();
	}

	public String parseEmpty(String json, List<String> replacements, String repl, List<String> tooltip, Player sender) {
		JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
		JsonArray array = obj.getAsJsonArray("extra");
		replaces = replacements;
		String regex = "";
		for (int i = 0; i < replacements.size(); ++i) {
			if (replacements.size() == 1) {
				regex = Pattern.quote(replacements.get(0));
				break;
			}
			if (i == 0 || i + 1 == replacements.size()) {
				if (i == 0) {
					regex = "(" + Pattern.quote(replacements.get(i));
				} else {
					regex = regex.concat("|").concat(Pattern.quote(replacements.get(i))).concat(")");
				}
				continue;
			}
			regex = regex.concat("|").concat(Pattern.quote(replacements.get(i)));
		}
		rgx = regex;
		JsonArray rep = new JsonArray();
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
		for (JsonElement ob : use)
			ob.getAsJsonObject().add("hoverEvent", hover);

		classicTooltip = use;

		for (int i = 0; i < array.size(); ++i) {
			if (array.get(i).isJsonObject()) {
				JsonObject o = array.get(i).getAsJsonObject();
				boolean inside = false;
				for (String replace : replacements)
					if (o.toString().contains(replace)) {
						if (inside) {
							break;
						}
						inside = true;
					}
				JsonElement text = o.get("text");
				if (text == null) {
					JsonElement el = o.get("extra");
					if (el != null) {
						JsonArray jar = el.getAsJsonArray();
						if (jar.size() != 0) {
							o.add("extra", parseNoItemArray(jar));
						} else {
							o.remove("extra");
						}
					}
					continue;
				} else {
					if (text.getAsString().isEmpty()) {
						JsonElement el = o.get("extra");
						if (el != null) {
							JsonArray jar = el.getAsJsonArray();
							if (jar.size() != 0) {
								o.add("extra", parseNoItemArray(jar));
							} else {
								o.remove("extra");
							}
						}
					}
				}

				String msg = text.getAsString();
				boolean isLast = false;
				for (String repls : replacements) {
					isLast = msg.endsWith(repls);
					if (isLast) {
						msg = msg.concat(".");
						break;
					}
				}
				String[] splits = msg.split(regex);
				if (splits.length != 1) {
					for (int j = 0; j < splits.length; ++j) {
						boolean endDot = (j == splits.length - 1) && isLast;
						if (!splits[j].isEmpty() && !endDot) {
							String st = o.toString();
							JsonObject fix = JsonParser.parseString(st).getAsJsonObject();
							fix.addProperty("text", splits[j]);
							rep.add(fix);
						}
						if (j != splits.length - 1) {
							rep.addAll(use);
						}
					}
				} else {
					rep.add(o);
				}
			} else {
				if (array.get(i).isJsonNull()) {
					continue;
				} else {
					if (array.get(i).isJsonArray()) {
						JsonArray jar = array.get(i).getAsJsonArray();
						if (jar.size() != 0) {
							rep.set(i, parseNoItemArray(array.get(i).getAsJsonArray()));
						}
					} else {

						String msg = array.get(i).getAsString();
						boolean isLast = false;
						for (String repls : replacements) {
							isLast = msg.endsWith(repls);
							if (isLast) {
								msg = msg.concat(".");
								break;
							}
						}
						String[] splits = msg.split(regex);
						if (splits.length != 1) {
							for (int j = 0; j < splits.length; ++j) {
								boolean endDot = (j == splits.length - 1) && isLast;
								if (!splits[j].isEmpty() && !endDot) {
									rep.add(new JsonPrimitive(splits[j]));
								}
								if (j != splits.length - 1) {
									rep.addAll(use);
								}
							}
						} else {
							rep.add(array.get(i));
						}
					}
				}
			}

		}
		obj.add("extra", rep);
		return obj.toString();
	}

	private JsonArray parseNoItemArray(JsonArray arr) {
		JsonArray replacer = new JsonArray();
		for (int i = 0; i < arr.size(); ++i) {
			if (arr.get(i).isJsonObject()) {
				JsonObject o = arr.get(i).getAsJsonObject();
				boolean inside = false;
				for (String replacement : replaces)
					if (o.toString().contains(replacement)) {
						if (inside) {
							break;
						}
						inside = true;
					}
				if (!inside) { // the placeholder we're looking for is not inside this element, so we continue
								// searching
					replacer.add(o);
					continue;
				}
				JsonElement text = o.get("text");
				if (text == null) {
					continue;
				}
				if (text.getAsString().isEmpty()) {
					JsonElement el = o.get("extra");
					if (el == null) {
						continue;
					}
					JsonArray jar = el.getAsJsonArray();
					if (jar.size() != 0) {
						o.add("extra", parseNoItemArray(jar));
					} else {
						o.remove("extra");
					}
				}

				String msg = text.getAsString();
				boolean isLast = false;
				for (String repls : replaces) {
					isLast = msg.endsWith(repls);
					if (isLast) {
						msg = msg.concat(".");
						break;
					}
				}
				String[] splits = msg.split(rgx);
				if (splits.length != 1) {
					for (int j = 0; j < splits.length; ++j) {
						boolean endDot = (j == splits.length - 1) && isLast;
						if (!splits[j].isEmpty() && !endDot) {
							String st = o.toString();
							JsonObject fix = JsonParser.parseString(st).getAsJsonObject();
							fix.addProperty("text", splits[j]);
							replacer.add(fix);
						}
						if (j != splits.length - 1) {
							replacer.addAll(classicTooltip);
						}
					}
				} else {
					replacer.add(o);
				}
			} else {
				if (arr.get(i).isJsonNull()) {
					continue;
				} else {
					if (arr.get(i).isJsonArray()) {
						JsonArray jar = arr.get(i).getAsJsonArray();
						if (jar.size() != 0) {
							jar = parseNoItemArray(arr.get(i).getAsJsonArray());
							replacer.set(i, jar);
						}
					} else {
						String msg = arr.get(i).getAsString();
						boolean isLast = false;
						for (String repls : replaces) {
							isLast = msg.endsWith(repls);
							if (isLast) {
								msg = msg.concat(".");
								break;
							}
						}
						String[] splits = msg.split(rgx);
						if (splits.length != 1) {
							for (int j = 0; j < splits.length; ++j) {
								boolean endDot = (j == splits.length - 1) && isLast;
								if (!splits[j].isEmpty() && !endDot) {
									replacer.add(new JsonPrimitive(splits[j]));
								}
								if (j != splits.length - 1) {
									replacer.addAll(classicTooltip);
								}
							}
						} else {
							replacer.add(arr.get(i));
						}
					}
				}
			}

		}
		return replacer;
	}

	private JsonArray parseArray(JsonArray arr) {
		JsonArray replacer = new JsonArray();
		for (int i = 0; i < arr.size(); ++i) {
			if (arr.get(i).isJsonObject()) {
				JsonObject o = arr.get(i).getAsJsonObject();
				boolean inside = false;
				for (String replacement : replaces)
					if (o.toString().contains(replacement)) {
						if (inside) {
							break;
						}
						inside = true;
					}
				if (!inside) { // the placeholder we're looking for is not inside this element, so we continue
								// searching
					replacer.add(o);
					continue;
				}
				JsonElement text = o.get("text");
				if (text == null) {
					continue;
				}
				if (text.getAsString().isEmpty()) {
					JsonElement el = o.get("extra");
					if (el == null) {
						continue;
					}
					JsonArray jar = el.getAsJsonArray();
					if (jar.size() != 0) {
						jar = parseArray(jar);
						o.add("extra", jar);
					} else {
						o.remove("extra");
					}
				}

				String msg = text.getAsString();
				boolean isLast = false;
				boolean done = false;
				boolean fnd;
				String[] splits;
				for (String repls : replaces) {
					if (done) {
						break;
					}
					isLast = msg.endsWith(repls);
					if (isLast) {
						done = true;
						msg = msg.concat(".");
					}
				}
				splits = msg.split(rgx);
				fnd = splits.length != 1;
				if (fnd)
					for (int j = 0; j < splits.length; ++j) {
						boolean endDot = (j == splits.length - 1) && isLast;
						if (!splits[j].isEmpty() && !endDot) {
							String st = o.toString();
							JsonObject fix = JsonParser.parseString(st).getAsJsonObject();
							fix.addProperty("text", splits[j]);
							replacer.add(fix);
						}
						if (j != splits.length - 1) {
							replacer.add(itemTooltip);
						}
					}
				if (!fnd) {
					replacer.add(o);
				}
			} else {
				if (arr.get(i).isJsonNull()) {
					continue;
				} else {
					if (arr.get(i).isJsonArray()) {
						JsonArray jar = arr.get(i).getAsJsonArray();
						if (jar.size() != 0) {
							jar = parseArray(arr.get(i).getAsJsonArray());
							replacer.set(i, jar);
						}
					} else {
						String msg = arr.get(i).getAsString();
						boolean isLast = false;
						boolean done = false;
						boolean fnd;
						String[] splits;
						for (String repls : replaces) {
							if (done) {
								break;
							}
							isLast = msg.endsWith(repls);
							if (isLast) {
								done = true;
								msg = msg.concat(".");
							}
						}
						splits = msg.split(rgx);
						fnd = splits.length != 1;
						if (fnd)
							for (int j = 0; j < splits.length; ++j) {
								boolean endDot = (j == splits.length - 1) && isLast;
								if (!splits[j].isEmpty() && !endDot) {
									JsonElement fix = new JsonPrimitive(splits[j]);
									replacer.add(fix);
								}
								if (j != splits.length - 1) {
									replacer.add(itemTooltip);
								}
							}
						if (!fnd) {
							replacer.add(arr.get(i));
						}
					}
				}
			}

		}
		return replacer;
	}

	@SuppressWarnings({ "deprecation" })
	public static String stringifyItem(JSONManipulatorCurrent j, ItemStack is, Version protocolVersion)
			throws Exception {
		Object nmsStack = JSONManipulatorCurrent.AS_NMS_COPY.invoke(null, is);
		Object nmsTag = JSONManipulatorCurrent.NBT_TAG_COMPOUND.newInstance();
		JSONManipulatorCurrent.SAVE_NMS_ITEM_STACK_METHOD.invoke(nmsStack, nmsTag);
		HashMap<String, String> tagMap = new HashMap<>();
		Map<String, Object> nmsMap = (Map<String, Object>) JSONManipulatorCurrent.MAP.get(nmsTag);
		String id = nmsMap.get("id").toString().replace("\"", "");
		Object realTag = nmsMap.get("tag");
		if (JSONManipulatorCurrent.NBT_TAG_COMPOUND.isInstance(realTag)) { // We need to make sure this is indeed an
																			// NBTTagCompound
			Map<String, Object> realMap = (Map<String, Object>) JSONManipulatorCurrent.MAP.get(realTag);
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
			if (!first)
				sb.append(",");
			first = false;
			if (!entry.getKey().isEmpty())
				sb.append(entry.getKey()).append(":");
			sb.append(cleanStr(entry.getValue()));
		}
		sb.append("}}"); // End of tag and end of item
		return sb.toString();
	}

	private static String cleanStr(String s) {
		return s.startsWith("'") && s.endsWith("'") ? s.substring(1, s.length() - 1) : s;
	}
}
