package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.hook.DiscordSrvSupport;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ReflectionUtils;
import me.dadus33.chatitem.utils.Utils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class AdventureComponentManager implements IComponentManager {
	
	@Override
	public boolean hasConditions() {
		try {
			for (String cl : Arrays.asList("net.kyori.adventure.text.Component", "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer"))
				Class.forName(cl);
		} catch (Throwable e) { // can't support this, adventure comp not found
			ChatItem.debug("Can't load AdventureComponentManager : " + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
	}

	public void writeComponentToPacket(ChatItemPacket packet, Component next) {
		if(packet.getContent().getSpecificModifier(Component.class).readSafely(0) == null && ReflectionUtils.hasObject(packet.getPacket(), "unsignedContent")) { // hard way
			ReflectionUtils.setField(ReflectionUtils.getObject(packet.getPacket(), "unsignedContent"), "adventure", next);
		} else { // easy way
			packet.getContent().getSpecificModifier(Component.class).write(0, next);
		}
	}

	public Component getComponentFromPacket(ChatItemPacket packet) {
		Component comp = packet.getContent().getSpecificModifier(Component.class).readSafely(0);
		if (comp == null && packet.getPacketName().equalsIgnoreCase("ClientboundPlayerChatPacket") && ReflectionUtils.hasObject(packet.getPacket(), "unsignedContent")) { // if can get one more
			comp = (Component) ReflectionUtils.getObject(ReflectionUtils.getObject(packet.getPacket(), "unsignedContent"), "adventure");
		}
		return comp;
	}
	
	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Component comp = getComponentFromPacket(packet);
		if(comp == null) // if comp stay null
			return null;
		try {
			String json = GsonComponentSerializer.gson().serialize(comp);
			ChatItem.debug("AdventureJSON : " + json);
			JsonObject jsonObj = JSONManipulator.parseOrGet(json);
			if (jsonObj.has("with")) {
				JsonObject next = new JsonObject();
				next.add("extra", jsonObj.get("with"));
				return next.toString();
			}
			return json;
		} catch (JsonParseException e) { // ignore this and just let skip this
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object manageContent(Player viewer, Chat chat, ChatItemPacket packet, String json, Storage c) throws Exception {
		ChatAction action = chat.getAction();
		if (action.isItem()) {
			ItemStack item = action.getItem();
			String itemName = ChatManager.getNameForChatAction(viewer, chat, c);
			ChatItem.debug("NBT tag: " + PacketUtils.getNbtTag(item));
			HoverEvent<?> hover;
			if(Utils.IS_PAPER)
				hover = item.asHoverEvent();
			else
				hover = HoverEvent.showItem(Key.key(item.getType().getKey().getKey()), item.getAmount(), BinaryTagHolder.of(PacketUtils.getNbtTag(item)));
			return manage(viewer, chat, packet, itemName, hover, null);
		}
		return manage(viewer, chat, packet, Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", chat.getPlayer().getName()),
				HoverEvent.showText(Component.text(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", chat.getPlayer().getName()))),
				ClickEvent.runCommand(action.getCommand()));
	}

	@Override
	public Object manageEmpty(Player viewer, Chat chat, ChatItemPacket packet, String json, Storage c) {
		Component builder = Component.text("");
		c.tooltipHand.forEach(s -> builder.append(Component.text(s)));
		ChatAction action = chat.getAction();
		if (action.isItem()) {
			return manage(viewer, chat, packet, ChatManager.getNameForChatAction(viewer, chat, c), HoverEvent.showText(builder), null);
		}
		return manage(viewer, chat, packet, Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", chat.getPlayer().getName()),
				HoverEvent.showText(Component.text(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", chat.getPlayer().getName()))),
				ClickEvent.runCommand(action.getCommand()));
	}

	private Object manage(Player viewer, Chat chat, ChatItemPacket packet, String replacement, HoverEvent<?> hover, ClickEvent click) {
		Component comp = getComponentFromPacket(packet);
		if (comp == null) {
			ChatItem.debug("The component is null.");
			return null;
		}
		comp = comp.replaceText(TextReplacementConfig.builder().matchLiteral(ChatManager.SEPARATOR + "" + chat.getId() + ChatManager.SEPARATOR_END).replacement(Component.text(replacement).hoverEvent(hover).clickEvent(click)).build());
		if(ChatItem.discordSrvSupport && DiscordSrvSupport.isSendingMessage() && viewer == chat.getPlayer())
			DiscordSrvSupport.sendChatMessage(chat.getPlayer(), comp, null);
		viewer.sendMessage(comp);
		return null;
	}
}
