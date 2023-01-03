package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent.ContentModifier;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Storage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class AdventureComponentManager implements IComponentManager {

	@Override
	public boolean hasConditions() {
		try {
			for (String cl : Arrays.asList("net.kyori.adventure.text.Component", "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer"))
				Class.forName(cl);
		} catch (Exception e) { // can't support this, adventure comp not found
			ChatItem.debug("Can't load AdventureComponentManager : " + e.getMessage());
			return false;
		}
		return true;
	}
	
	@Override
	public void writeJson(ChatItemPacket packet, String json) {}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Component comp = packet.getContent().getSpecificModifier(Component.class).readSafely(0);
		if (comp == null)
			return null;
		String json = GsonComponentSerializer.gson().serialize(comp);
		JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
		ChatItem.debug("AdventureJSON : " + json);
		if (jsonObj.has("with")) {
			JsonObject next = new JsonObject();
			next.add("extra", jsonObj.get("with"));
			return next.toString();
		}
		return json;
	}

	@Override
	public Object manageItem(Player p, Chat chat, ChatItemPacket packet, ItemStack item, Storage c) throws Exception {
		String itemName = ChatManager.getNameOfItem(chat.getPlayer(), item, c);
		return manage(p, chat, packet, itemName, HoverEvent.showItem(Key.key(item.getType().getKey().getKey()), item.getAmount(), BinaryTagHolder.of(PacketUtils.getNbtTag(item))));
	}

	@Override
	public Object manageEmpty(Player p, Chat chat, ChatItemPacket packet, Storage c) {
		Component builder = Component.text("");
		c.tooltipHand.forEach(s -> builder.append(Component.text(s)));
		Player sender = chat.getPlayer();
		String handName = c.handName.replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName());
		return manage(p, chat, packet, handName, HoverEvent.showText(builder));
	}

	private Object manage(Player p, Chat chat, ChatItemPacket packet, String replacement, HoverEvent<?> hover) {
		ContentModifier<Component> modifier = packet.getContent().getSpecificModifier(Component.class);
		Component comp = modifier.readSafely(0);
		if (comp == null) {
			ChatItem.debug("The component is null.");
			return null;
		}
		comp = checkComponent(comp, hover, replacement, chat);
		ChatItem.debug("Result: " + GsonComponentSerializer.gson().serialize(comp));
		modifier.write(0, comp);
		packet.setPacket(modifier.getObj());
		return packet.getPacket();
	}

	private Component checkComponent(Component comp, HoverEvent<?> hover, String itemName, Chat chat) {
		if (comp instanceof TextComponent) {
			TextComponent tc = (TextComponent) comp;
			if (ChatManager.containsSeparator(tc.content())) {
				ChatItem.debug("Changing text " + tc.content() + " to " + itemName);
				comp = tc.content(ChatManager.replaceSeparator(chat, tc.content(), itemName)).hoverEvent(hover);
			} else
				ChatItem.debug("No insert of text without separator: " + tc.content());
		} else if(comp instanceof TranslatableComponent) {
			TranslatableComponent tc = (TranslatableComponent) comp;
			List<Component> next = new ArrayList<>();
			for (Component extra : tc.args()) {
				next.add(checkComponent(extra, hover, itemName, chat));
			}
			comp = tc.args(next);
		} else
			ChatItem.debug("Not valid comp class " + comp.getClass().getSimpleName() + " : " + comp);
		List<Component> next = new ArrayList<>();
		for (Component extra : comp.children()) {
			next.add(checkComponent(extra, hover, itemName, chat));
		}
		return comp.children(next);
	}
}
