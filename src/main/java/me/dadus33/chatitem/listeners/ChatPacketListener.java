package me.dadus33.chatitem.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;


public class ChatPacketListener extends PacketAdapter {

    private final static String NAME = "{name}";
    private final static String AMOUNT = "{amount}";
    private final static String TIMES = "{times}";
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
        if(ChatItem.post17)
        if (e.getPacket().getBytes().readSafely(0) == (byte) 2) {
            return;  //It means it's an actionbar message, and we ain't intercepting those
        }

        PacketContainer packet = e.getPacket();
        String json = packet.getChatComponents().readSafely(0).getJson();

        boolean found = false;
        for (String rep : c.PLACEHOLDERS)
            if (json.contains(rep)) {
                found = true;
                break;
            }
        if (!found) {
            return; //then it's just a normal message without placeholders, so we leave it alone
        }
        int topIndex = -1;
        String name = null;
        for(Player p : Bukkit.getOnlinePlayers()){
            String pname = p.getName();
            if(!json.contains(pname)){
                continue;
            }
            int index = json.lastIndexOf(pname)+pname.length();
            if(index>topIndex){
                topIndex = index;
                name = pname;
            }
        }
        if(name==null){ //something went really bad, so we run away and hide
            return;
        }
        Player p = Bukkit.getPlayer(name);
        StringBuilder buff = new StringBuilder(json);
        buff.replace(topIndex-name.length(), topIndex, "");
        json = buff.toString();


        //STYLE BEGIN

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
                    amount = amount.replace(TIMES, "1");
                    replacer = replacer.replace(AMOUNT, amount);
                } else {
                    replacer = replacer.replace(AMOUNT, "");
                }
            } else {
                amount = amount.replace(TIMES, String.valueOf(inHand.getAmount()));
                replacer = replacer.replace(AMOUNT, amount);
            }
            if (dname) {
                String trp = inHand.getItemMeta().getDisplayName();
                replacer = replacer.replace(NAME, trp);
            } else {
                HashMap<Short, String> translationSection = c.TRANSLATIONS.get(inHand.getType().name());
                if(translationSection==null){
                    String trp = materialToName(inHand.getType());
                    replacer = replacer.replace(NAME, trp);
                }else {
                    String translated = translationSection.get(inHand.getDurability());
                    if (translated != null) {
                        replacer = replacer.replace(NAME, translated);
                    } else {
                        replacer = replacer.replace(NAME, materialToName(inHand.getType()));
                    }
                }
            }


        //STYLE END

        String[] reps = new String[c.PLACEHOLDERS.size()];
        c.PLACEHOLDERS.toArray(reps);
        String message = null;
        try {
            if(!p.getItemInHand().getType().equals(Material.AIR))
            message = ChatItem.getManipulator().parse(json, reps, p.getItemInHand(), replacer);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e1) {
            e1.printStackTrace();
        }
        if(message!=null)
            packet.getChatComponents().writeSafely(0, WrappedChatComponent.fromJson(message));
        else
            e.setCancelled(true);

    }

    public void setStorage(Storage st) {
        c = st;
    }


}
