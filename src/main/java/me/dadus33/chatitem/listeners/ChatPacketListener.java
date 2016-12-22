package me.dadus33.chatitem.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.Storage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
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
        if(ChatItem.mcSupportsActionBar()) //only if action mar messages are supported in this version of minecraft
        if (e.getPacket().getBytes().readSafely(0) == (byte) 2) {
            return;  //It means it's an actionbar message, and we ain't intercepting those
        }
        boolean usesChatComponent = false;
        PacketContainer packet = e.getPacket();
        String json;
        if(packet.getChatComponents().readSafely(0)==null){  //null check for some cases of messages sent using spigot's Chat Component API or other means
            if(ChatItem.supportsChatComponentApi()){  //only if the API is supported in this server distribution
                BaseComponent[] components = packet.getSpecificModifier(BaseComponent[].class).readSafely(0);
                if(components == null){
                    return;
                }
                json = ComponentSerializer.toString(components);
                usesChatComponent = true;
            }else{
                return;
            }
        }else{
            json = packet.getChatComponents().readSafely(0).getJson();
        }


        boolean found = false;
        for (String rep : c.PLACEHOLDERS)
            if (json.contains(rep)) {
                found = true;
                break;
            }
        if (!found) {
            return; //then it's just a normal message without placeholders, so we leave it alone
        }
        if(json.lastIndexOf("\\u0007")==-1){ //if the message doesn't contain the BELL separator, then it's certainly NOT a message we want to parse
            return;
        }

        //here we find the last player name in the string that has a BELL character before it
        int topIndex = -1;
        String name = null;
        for(Player p : Bukkit.getOnlinePlayers()){
            String pname = "\\u0007"+p.getName();
            if(!json.contains(pname)){
                continue;
            }
            int index = json.lastIndexOf(pname)+pname.length();
            if(index>topIndex){
                topIndex = index;
                name = pname.replace("\\u0007", "");
            }
        }
        if(name==null){ //something went really bad, so we run away and hide
            return;
        }

        Player p = Bukkit.getPlayer(name);
        StringBuilder buff = new StringBuilder(json);
        buff.replace(topIndex-(name.length()+6), topIndex, ""); //we remove both the name and the separator from the json string
        json = buff.toString();


        //Item Styling
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
            if(!p.getItemInHand().getType().equals(Material.AIR)) {
                ItemStack hand = p.getItemInHand();
                if(hand.getType().equals(Material.BOOK_AND_QUILL) || hand.getType().equals(Material.WRITTEN_BOOK)){
                    BookMeta bm = (BookMeta)hand.getItemMeta();
                    bm.setPages(Collections.<String>emptyList());
                    hand.setItemMeta(bm);
                }
                message = ChatItem.getManipulator().parse(json, reps, hand, replacer);
            }
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e1) {
            e1.printStackTrace();
        }
        if(message!=null) {
            if(!usesChatComponent) {
                packet.getChatComponents().writeSafely(0, WrappedChatComponent.fromJson(message));
            }else{
                packet.getSpecificModifier(BaseComponent[].class).writeSafely(0, ComponentSerializer.parse(message));
            }
        }
        else {
            e.setCancelled(true);
        }
    }

    public void setStorage(Storage st) {
        c = st;
    }


}
