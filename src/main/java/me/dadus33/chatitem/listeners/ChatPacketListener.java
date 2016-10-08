package me.dadus33.chatitem.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
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

    final static HashMap<Long, String> SENDERS = new HashMap<>();
    private final static String NAME = Pattern.quote("{name}");
    private final static String AMOUNT = Pattern.quote("{amount}");
    private final static String TIMES = Pattern.quote("{times}");
    private ChatItem instance;
    private Storage c;

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

    @SuppressWarnings("deprecation")
    @Override
    public void onPacketSending(PacketEvent e) {
        if (e.getPacket().getBytes().readSafely(0) == (byte) 2) {
            return;  //It means it's an actionbar message, and we ain't intercepting those
        }
        String json = e.getPacket().getChatComponents().readSafely(0).getJson();
        boolean found = false;
        for (String rep : c.PLACEHOLDERS)
            if (json.contains(rep)) {
                found = true;
                break;
            }
        if (!found) {
            return; //then it's just a normal message without placeholders, so we leave it alone
        }
        PacketContainer packet = e.getPacket();
        if (packet.getMetadata("goodPacket") == null) { //if this isn't one of the already-parsed packets
            String playerName = null;
            Long currentTime = System.currentTimeMillis();
            Long minDiff = 10000L;
            Map.Entry entry = null;
            for (Map.Entry<Long, String> ent : SENDERS.entrySet()) {
                long dif = currentTime - ent.getKey();
                if (dif <= minDiff) {
                    minDiff = dif;
                    playerName = ent.getValue();
                    entry = ent;
                }
            } //we searched for the player who sent the packet
            e.setCancelled(true); //we cancel this as we're going to manually resend all packets anyways
            if (playerName == null) {
                return;
            }
            Player p = Bukkit.getPlayer(playerName);
            SENDERS.entrySet().remove(entry);


            //Styling begin


            ItemStack inHand = p.getItemInHand();
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
                String translated = c.TRANSLATIONS.get(inHand.getType().name()).get(inHand.getDurability());
                if (translated != null) {
                    replacer = replacer.replaceAll(NAME, translated);
                } else {
                    replacer = replacer.replaceAll(NAME, materialToName(inHand.getType()));
                }
            }
            //Styling end
            String[] reps = new String[c.PLACEHOLDERS.size()];
            c.PLACEHOLDERS.toArray(reps);
            String message = null;
            try {
                message = JSONManipulator.parse(json, reps, p.getItemInHand(), replacer);
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e1) {
                e1.printStackTrace();
            }
            packet.getChatComponents().writeSafely(0, WrappedChatComponent.fromJson(message));
            packet.addMetadata("goodPacket", true); //Adding this metadata will let it pass through our filter
            for (Player player : Bukkit.getOnlinePlayers()) { //And now we send it to all players on the server
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, true);
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            }

        }

    }

    public void setStorage(Storage st) {
        c = st;
    }


}
