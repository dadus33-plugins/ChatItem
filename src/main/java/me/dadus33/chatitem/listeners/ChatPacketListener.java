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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class ChatPacketListener extends PacketAdapter {

    public final static HashMap<Long, String> SENDERS = new HashMap<>();
    private final static String NAME = Pattern.quote("{name}");
    private final static String AMOUNT = Pattern.quote("{amount}");
    private final static String TIMES = Pattern.quote("{times}");
    private final static HashMap<String, Integer> PLAYER = new HashMap<>();
    ChatItem instance;
    Storage c;

    public ChatPacketListener(Plugin plugin, ListenerPriority listenerPriority, Storage s, PacketType... types) {
        super(plugin, listenerPriority, types);
        this.instance = (ChatItem) plugin;
        c = s;
    }

    private static String materialToName(Material m) {
        if (m.equals(Material.TNT)) {
            return "TNT";
        }
        String orig = m.toString().toLowerCase();
        String[] splits = orig.split("_");
        StringBuilder sb = new StringBuilder(orig.length());
        int pos = 0;
        for (String split : splits) {
            sb.append(split);
            int loc = sb.lastIndexOf(split);
            char charLoc = sb.charAt(loc);
            if (!(split.equalsIgnoreCase("of") || split.equalsIgnoreCase("and") ||
                    split.equalsIgnoreCase("with") || split.equalsIgnoreCase("on")))
                sb.setCharAt(loc, Character.toUpperCase(charLoc));
            if (pos != splits.length - 1)
                sb.append(' ');
            ++pos;
        }

        return sb.toString();


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
        Player p;
        String pname = "";
        long diff = 1000;
        for (Map.Entry<Long, String> entry : SENDERS.entrySet()) {
            long smallst = System.currentTimeMillis() - entry.getKey();
            if (smallst < diff) {
                diff = smallst;
                pname = entry.getValue();
            }
        }
        p = Bukkit.getPlayer(pname);
        if (!p.hasPermission("chatitem.use")) {
            e.setCancelled(true);
            return;
        }
        String message = json;
        String[] reps = new String[c.PLACEHOLDERS.size()];
        c.PLACEHOLDERS.toArray(reps);
        ItemStack inHand = p.getItemInHand();
        if (inHand.getType() == Material.AIR) {
            if (c.DENY_IF_NO_ITEM) {
                if (!c.DENY_MESSAGE.isEmpty()) {
                    if (!PLAYER.containsKey(p.getName()))
                        p.sendMessage(c.DENY_MESSAGE);
                    if (PLAYER.containsKey(p.getName())) {
                        if (PLAYER.get(p.getName()) == 1) {
                            PLAYER.remove(p.getName());
                        } else {
                            PLAYER.put(p.getName(), PLAYER.get(p.getName()) - 1);
                        }
                    } else {
                        PLAYER.put(p.getName(), Bukkit.getOnlinePlayers().size());
                    }
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
        } else {
            if (inHand.hasItemMeta()) {
                if (inHand.getItemMeta().hasDisplayName()) {
                    dname = true;
                }
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
                replacer = replacer.replaceAll(NAME, materialToName(inHand.getType()));
            }
        }

        try {
            message = JSONManipulator.parse(json, reps, p.getItemInHand(), replacer);
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
