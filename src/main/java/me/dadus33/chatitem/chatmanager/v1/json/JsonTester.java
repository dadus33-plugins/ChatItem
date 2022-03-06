package me.dadus33.chatitem.chatmanager.v1.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

public class JsonTester {

	@SuppressWarnings({ "unchecked", "deprecation" })
	public static String stringifyItem2(JSONManipulatorCurrent j, ItemStack is) throws Exception {
		Object nmsStack = JSONManipulatorCurrent.AS_NMS_COPY.invoke(null, is);
		Object nmsTag = JSONManipulatorCurrent.NBT_TAG_COMPOUND.newInstance();
		JSONManipulatorCurrent.SAVE_NMS_ITEM_STACK_METHOD.invoke(nmsStack, nmsTag);
		HashMap<String, String> tagMap = new HashMap<>();
		Map<String, Object> nmsMap = (Map<String, Object>) JSONManipulatorCurrent.MAP.get(nmsTag);
		String id = nmsMap.get("id").toString().replace("\"", "");
		Object realTag = nmsMap.get("tag");
		if (JSONManipulatorCurrent.NBT_TAG_COMPOUND.isInstance(realTag)) { // We need to make sure this is indeed an NBTTagCompound
			Map<String, Object> realMap = (Map<String, Object>) JSONManipulatorCurrent.MAP.get(realTag);
			Set<Map.Entry<String, Object>> entrySet = realMap.entrySet();
			for (Map.Entry<String, Object> entry : entrySet) {
				tagMap.put(entry.getKey(), entry.getValue().toString());
			}
		}

		//ItemRewriter.remapIds(Version.getVersion().MAX_VER, JSONManipulatorCurrent.protocolVersion.MAX_VER, item);
		StringBuilder sb = new StringBuilder("{id:");
		sb.append("\"").append(id).append("\"").append(","); // Append the id
		sb.append("Count:").append(is.getAmount()).append("b"); // Append the amount
		
		if(!tagMap.containsKey("Damage")) { // for new versions
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
			if(!first)
				sb.append(",");
			first = false;
			if(!entry.getKey().isEmpty())
				sb.append(entry.getKey()).append(":");
			sb.append(cleanStr(entry.getValue()));
		}
		sb.append("}}"); // End of tag and end of item
		return sb.toString();
	}
	
	private static String cleanStr(String s) {
		if(s.startsWith("'"))
			return s.substring(1);
		if(s.endsWith("'"))
			return s.substring(0, s.length() - 1);
		return s;
	}
}
