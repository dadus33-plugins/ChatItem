package me.dadus33.chatitem.platform.hook;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemPlayer;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.platform.IPlatform;
import me.dadus33.chatitem.playernamer.PlayerNamerManager;
import me.dadus33.chatitem.utils.ColorManager;
import me.dadus33.chatitem.utils.Colors;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ReflectionUtils;
import me.dadus33.chatitem.utils.Utils;
import me.dadus33.chatitem.utils.Version;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

@SuppressWarnings("deprecation")
public class SpigotPlatform implements IPlatform {

	private static boolean shouldUseAppendMethod = false;

	static {
		try {
			try {
				ComponentBuilder.class.getDeclaredMethod("append", BaseComponent[].class);
				shouldUseAppendMethod = true;
			} catch (Exception e) {
				shouldUseAppendMethod = false;
			}
			ChatItem.getInstance().getLogger().info(shouldUseAppendMethod ? "Use ComponentBuilder's method." : "Use own ComponentBuilder append method.");
		} catch (Exception e) {

		}
	}

	@Override
	public String getName() {
		return "Spigot";
	}

	@Override
	public Inventory createInventory(InventoryHolder holder, int slot, String name) {
		return Bukkit.createInventory(holder, slot, name);
	}

	@Override
	public ItemStack createItemStack(Material type, String name) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.setDisplayName(Colors.RESET + name);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public ItemStack createItemStack(Material type, String name, List<String> lore) {
		ItemStack item = new ItemStack(type);
		ItemMeta meta = (ItemMeta) item.getItemMeta();
		meta.setDisplayName(Colors.RESET + name);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public String getItemDisplayName(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
	}

	@Override
	public String getPluginVersion(Plugin plugin) {
		return plugin.getDescription().getVersion();
	}

	@Override
	public Version getMinecraftVersion() {
		return Version.getVersionByName(getNMSVersion().replace("_R4", "_6"));
	}

	@Override
	public String getNMSVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	@Override
	public boolean hasBaseComponentSerializer() {
		return getBaseComponentToJsonMethod() != null;
	}

	@Override
	public String baseComponentToJson(Object obj) {
		Method m = getBaseComponentToJsonMethod();
		try {
			Object[] args = new Object[m.getParameterCount()];
			args[0] = obj;
			return (String) m.invoke(null, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object jsonToBaseComponent(String json) {
		Method m = getJsonToBaseComponentMethod();
		try {
			Object[] args = new Object[m.getParameterCount()];
			args[0] = json;
			return m.invoke(null, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Method getBaseComponentToJsonMethod() {
		Class<?> chatSerializerClass = PacketUtils.getNmsClass("IChatBaseComponent$ChatSerializer", "network.chat.", "ChatSerializer", "Component$Serializer");
		Class<?> chatBaseComponentClass = PacketUtils.getNmsClass("IChatBaseComponent", "network.chat.", "Component");
		if (chatSerializerClass == null || chatBaseComponentClass == null)
			return null;
		try {
			for (Method m : chatSerializerClass.getDeclaredMethods()) {
				if (m.getParameterTypes()[0].equals(chatBaseComponentClass) && m.getReturnType().equals(String.class)) {
					return m;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Method getJsonToBaseComponentMethod() {
		Class<?> chatSerializerClass = PacketUtils.getNmsClass("IChatBaseComponent$ChatSerializer", "network.chat.", "ChatSerializer", "Component$Serializer");
		Class<?> chatBaseComponentClass = PacketUtils.getNmsClass("IChatBaseComponent", "network.chat.", "Component");
		Class<?> chatMutableComponentClass = PacketUtils.getNmsClass("IChatMutableComponent", "network.chat.");
		if (chatSerializerClass == null || chatBaseComponentClass == null)
			return null;
		try {
			for (Method m : chatSerializerClass.getDeclaredMethods()) {
				if (m.getParameterCount() == 0)
					continue;
				if (m.getParameterTypes()[0].equals(String.class)
						&& (m.getReturnType().isAssignableFrom(chatBaseComponentClass) || (chatMutableComponentClass != null && m.getReturnType().equals(chatMutableComponentClass)))) {
					m.setAccessible(true);
					return m;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void sendMessage(Player to, Player origin, ChatAction action, String msg) {
		ComponentBuilder builder = new ComponentBuilder("");
		ChatColor color = ChatColor.WHITE;
		String colorCode = "", text = "";
		boolean waiting = false, removing = false;
		for (char args : msg.toCharArray()) {
			if (args == '§') { // begin of color
				if (colorCode.isEmpty() && !text.isEmpty()) { // text before this char
					if (text.length() > 2 && text.startsWith("§") && text.substring(2) == ChatColor.stripColor(text) && color != null && color != ChatColor.WHITE) {
						text = text.substring(2); // remove some code which should not be here
					}
					appendToComponentBuilder(builder, createComponent(to, text, color, action));
					text = "";
				}
				waiting = true; // waiting for color code
			} else if (waiting) { // if waiting for code and valid str
				// if it's hexademical value and with enough space for full color
				waiting = false;
				if (args == 'r' && colorCode.isEmpty()) {
					color = ChatColor.RESET;
					continue;
				} else if (args == 'x') {
					if (!colorCode.isEmpty()) {
						color = ColorManager.getColor(colorCode);
						colorCode = ""; // clean for previous things
					}
				}
				colorCode += args;
			} else {
				waiting = false;
				if (!colorCode.isEmpty()) {
					if (colorCode.startsWith("x") && colorCode.length() >= 7) {
						if (colorCode.length() == 7)
							color = ColorManager.getColor(colorCode);
						else {
							color = ColorManager.getColor(colorCode.substring(0, 7)); // only the hex code
							text += ColorManager.getColorString(colorCode.substring(7, colorCode.length()));
						}
					} else if (colorCode.length() == 1) // if only one color code
						color = ColorManager.getColor(colorCode);
					else
						text += ColorManager.getColorString(colorCode);
					colorCode = "";
				}
				if (args == ChatManager.SEPARATOR) {
					// here put the item
					appendToComponentBuilder(builder, fixColorComponent(to, text, color, action));
					if (action.isItem())
						addItem(builder, to, origin, action.getItem(), action);
					else
						addCommand(builder, to, origin, action.getCommand(), action);
					text = "";
					if (ChatManager.containsSeparatorEnd(msg))
						removing = true;
				} else if (args == ChatManager.SEPARATOR_END) {
					removing = false;
				} else if (!removing) { // not removing content
					// basic text, not waiting for code after '§'
					text += args;
				}
			}
		}
		if (!text.isEmpty())
			appendToComponentBuilder(builder, createComponent(to, text, color, action));
		to.spigot().sendMessage(builder.create());
	}

	public static void addItem(ComponentBuilder builder, Player to, Player origin, ItemStack item, ChatAction action) {
		Storage c = ChatItem.getInstance().getStorage();
		if (!ItemUtils.isEmpty(item)) {
			ComponentBuilder itemComponent = new ComponentBuilder("");
			appendToComponentBuilder(itemComponent, fixColorComponent(to, ChatManager.getNameOfItem(origin, item, to, c), ChatColor.WHITE, action));
			ChatItem.debug("Item for " + to.getName() + " (ver: " + ItemPlayer.getPlayer(to).getVersion().name() + ") : " + PacketUtils.getNbtTag(item));
			// itemComponent.event(new HoverEvent(Action.SHOW_ITEM, itemBaseComponent));
			appendToComponentBuilder(builder, itemComponent.create());
		} else {
			String handName = c.handName;
			ComponentBuilder handComp = new ComponentBuilder("");
			ComponentBuilder handTooltip = new ComponentBuilder("");
			int stay = c.tooltipHand.size();
			for (String line : c.tooltipHand) {
				stay--;
				handTooltip.append(ColorManager.fixColor(line.replace("{name}", origin.getName()).replace("{display-name}", origin.getDisplayName())));
				if (stay > 0)
					handTooltip.append("\n");
			}
			handComp.event(Utils.createTextHover(handTooltip.create()));
			if (handName.contains("{display-name}")) {
				String[] splitted = handName.split("\\{display-name\\}");
				for (int i = 0; i < (splitted.length - 1); i++) {
					handComp.append(splitted[i]);
					appendToComponentBuilder(handComp, PlayerNamerManager.getPlayerNamer().getName(origin));
				}
				handComp.append(splitted[splitted.length - 1]);
			} else
				handComp.append(handName.replace("{name}", origin.getName()));
			appendToComponentBuilder(builder, handComp.create());
		}
	}

	public static void addCommand(ComponentBuilder builder, Player to, Player origin, String command, ChatAction action) {
		ComponentBuilder itemComponent = new ComponentBuilder("");
		appendToComponentBuilder(itemComponent, fixColorComponent(to, Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", origin.getName()), ChatColor.WHITE, action));
		appendToComponentBuilder(builder, itemComponent.create());
	}

	public static void appendToComponentBuilder(ComponentBuilder builder, BaseComponent[] comps) {
		if (shouldUseAppendMethod) {
			try {
				builder.append(comps);
			} catch (Exception e) {
				ChatItem.getInstance().getLogger().severe("This should NEVER append. The ComponentBuilder#append(BaseComponent[]) was found but it's NOT. Using own next time.");
				shouldUseAppendMethod = false;
			}
		} else {
			try {
				Field currentField = ReflectionUtils.getField(builder, "current");
				List<BaseComponent> parts = (List<BaseComponent>) ReflectionUtils.getObject(builder, "parts");
				parts.add(new TextComponent((TextComponent) currentField.get(builder)));
				parts.addAll(Arrays.asList(comps));
				currentField.set(builder, new TextComponent(""));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static BaseComponent[] fixColorComponent(Player to, String message, ChatColor color, ChatAction action) {
		ComponentBuilder builder = new ComponentBuilder("");
		String colorCode = "", text = "";
		boolean waiting = false;
		for (char args : message.toCharArray()) {
			if (args == '§') { // begin of color
				if (colorCode.isEmpty() && !text.isEmpty()) { // text before this char
					ChatItem.debug("Append while fixing name " + (ColorManager.isHexColor(color) && builder.getParts().isEmpty() ? ColorManager.removeColorAtBegin(text) : text));
					appendToComponentBuilder(builder, createComponent(to, ColorManager.isHexColor(color) ? ColorManager.removeColorAtBegin(text) : text, color, action));
					text = "";
				}
				waiting = true; // waiting for color code
			} else if (waiting) { // if waiting for code and valid str
				// if it's hexademical value and with enough space for full color
				waiting = false;
				if (args == 'r' && colorCode.isEmpty()) {
					color = ChatColor.RESET;
					continue;
				}
				if (args == 'x' && !colorCode.isEmpty()) {
					text += ColorManager.getColorString(colorCode);
					colorCode = "x";
				} else
					colorCode += args; // a color by itself
			} else {
				waiting = false;
				if (!colorCode.isEmpty()) {
					if (colorCode.startsWith("x") && colorCode.length() >= 7) {
						if (colorCode.length() == 7)
							color = ColorManager.getColor(colorCode);
						else {
							color = ColorManager.getColor(colorCode.substring(0, 7)); // only the hex code
							text += ColorManager.getColorString(colorCode.substring(7, colorCode.length()));
						}
					} else if (colorCode.length() == 1) // if only one color code
						color = ColorManager.getColor(colorCode);
					else
						text += ColorManager.getColorString(colorCode);
					colorCode = "";
				}
				// basic text, not waiting for code after '§'
				text += args;
			}
		}
		if (!text.isEmpty()) {
			appendToComponentBuilder(builder, createComponent(to, text, color, action));
		}
		return builder.create();
	}

	private static BaseComponent[] createComponent(Player to, String text, ChatColor color, ChatAction action) {
		ComponentBuilder littleBuilder = new ComponentBuilder(ChatItem.replace(action.getOrigin(), text));
		if (color != null && !Colors.isFormatting(color)) // don't add reset thing
			littleBuilder.color(color);
		if (action.isItem()) {
			if (action.getItem().getType().equals(Material.AIR))
				littleBuilder.event(Utils.createTextHover(String.join("\n", ChatItem.getInstance().getStorage().tooltipHand)));
			else
				littleBuilder.event(Utils.createItemHover(action.getItem(), to));
		} else {
			littleBuilder.event(Utils.createTextHover(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover")));
			if (action.hasCommand())
				littleBuilder.event(Utils.createRunCommand(action.getCommand()));
		}
		return littleBuilder.create();
	}

	private static final List<String> SKIPPED = Arrays.asList("HSTRY_ENCHANTS");

	@Override
	public String stringifyItem(ItemStack item) {
		try {
			ChatItem.debug("[JSONManipulator] stringifying item");
			Object nmsStack = JSONManipulator.AS_NMS_COPY.invoke(null, item);
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
			sb.append("Count:").append(item.getAmount()).append("b"); // Append the amount

			if (!tagMap.containsKey("Damage")) { // for new versions
				sb.append(",Damage:").append(item.getDurability()).append("s"); // Append the durability data
			}
			if (tagMap.isEmpty()) {
				sb.append("}");
				return sb.toString();
			}
			Set<Map.Entry<String, String>> entrySet = tagMap.entrySet();
			boolean first = true;
			sb.append(",tag:{"); // Start of the tag
			for (Map.Entry<String, String> entry : entrySet) {
				if (SKIPPED.contains(entry.getKey()))
					continue;
				if (!first)
					sb.append(",");
				first = false;
				if (!entry.getKey().isEmpty()) {
					if (entry.getKey().contains(";"))
						sb.append("\"" + entry.getKey() + "\"");
					else
						sb.append(entry.getKey());
					sb.append(":");
				}
				sb.append(Utils.cleanStr(entry.getValue()));
			}
			sb.append("}}"); // End of tag and end of item
			return sb.toString();
		} catch (Exception e) {
			return "{}";
		}
	}
}
