package me.dadus33.chatitem.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.JSONManipulator;
import me.dadus33.chatitem.utils.Storage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;


public class ChatPacketListener extends PacketAdapter {

    public final static String NAME = Pattern.quote("{name}");
    public final static String AMOUNT = Pattern.quote("{amount}");
    public final static String TIMES = Pattern.quote("{times}");
    ChatItem instance;
    Storage c;

    public ChatPacketListener(Plugin plugin, ListenerPriority listenerPriority, Storage s, PacketType... types) {
        super(plugin, listenerPriority, types);
        this.instance = (ChatItem) plugin;
        c = s;
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        PacketContainer packet = e.getPacket();
        if (packet.getBytes().readSafely(0) == (byte) 2) {
            return;
        }
        WrappedChatComponent chatMessage = packet.getChatComponents().readSafely(0);
        String json = chatMessage.getJson();
        boolean found = false;
        for (int i = 0; i < c.PLACEHOLDERS.size(); ++i) {
            if (json.contains(c.PLACEHOLDERS.get(i))) {
                found = true;
                break;
            }
        }
        if (!found) {
            return;
        }
        if (!e.getPlayer().hasPermission("chatitem.use")) {
            e.setCancelled(true);
            return;
        }
        String message = json;
        String[] reps = new String[c.PLACEHOLDERS.size()];
        c.PLACEHOLDERS.toArray(reps);
        ItemStack inHand = e.getPlayer().getItemInHand();
        if (inHand.getType() == Material.AIR) {
            if (c.DENY_IF_NO_ITEM) {
                if (!c.DENY_MESSAGE.isEmpty()) {
                    e.getPlayer().sendMessage(c.DENY_MESSAGE);
                }
                e.setCancelled(true);
                return;
            }
            return;
        }
        String replacer = c.NAME_FORMAT;
        String amount = c.AMOUNT_FORMAT;
        boolean dname = false;
        if (!c.COLOR_IF_ALREADY_COLORED && inHand.hasItemMeta()) {
            if (inHand.getItemMeta().hasDisplayName()) {
                replacer = ChatColor.stripColor(replacer);
                dname = true;
            }
        }
        if (inHand.getAmount() == 1) {
            if (c.FORCE_ADD_AMOUNT) {
                amount = amount.replaceAll(TIMES, "1");
                replacer = replacer.replaceAll(AMOUNT, amount);
            } else {
                replacer = replacer.replaceAll(AMOUNT, "");
            }
        } else {
            amount = amount.replaceAll(TIMES, String.valueOf(inHand.getAmount()));
            replacer = replacer.replaceAll(AMOUNT, amount);
        }
        if (dname) {
            replacer = replacer.replaceAll(NAME, inHand.getItemMeta().getDisplayName());
        } else {
            String translated = c.TRANSLATIONS.get(inHand.getType().name().concat(":").concat(String.valueOf(inHand.getDurability())));
            if (translated != null) {
                replacer = replacer.replaceAll(NAME, translated);
            } else {
                StringBuilder stringBuilder = new StringBuilder(inHand.getType().toString().toLowerCase());
                stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
                replacer = replacer.replaceAll(NAME, stringBuilder.toString().replaceAll("_", " "));
            }
        }

        try {
            message = JSONManipulator.parse(json, reps, e.getPlayer().getItemInHand(), replacer);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e1) {
            e1.printStackTrace();
        }
        if (message != null) {
            packet.getChatComponents().writeSafely(0, WrappedChatComponent.fromJson(message));
        }
    }


    public void setStorage(Storage st) {
        c = st;
    }


}
