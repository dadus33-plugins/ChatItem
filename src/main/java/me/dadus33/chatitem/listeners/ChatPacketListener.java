package me.dadus33.chatitem.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.ProtocolVersion;
import me.dadus33.chatitem.utils.Storage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class ChatPacketListener extends PacketAdapter {

    private final static String NAME = "{name}";
    private final static String AMOUNT = "{amount}";
    private final static String TIMES = "{times}";
    private final static List<Material> SHULKER_BOXES = new ArrayList<>();

    private Storage c;


    public ChatPacketListener(Plugin plugin, ListenerPriority listenerPriority, Storage s, PacketType... types) {
        super(plugin, listenerPriority, types);
        if(ChatItem.supportsShulkerBoxes()){
            SHULKER_BOXES.addAll(Arrays.asList(Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
                    Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
                    Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
                    Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.SILVER_SHULKER_BOX,
                    Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX));
        }
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

    private void stripData(ItemStack i){
        if(i == null){
            return;
        }
        if(i.getType().equals(Material.AIR)){
            return;
        }
        if(!i.hasItemMeta()){
            return;
        }
        ItemMeta im = Bukkit.getItemFactory().getItemMeta(i.getType());
        ItemMeta original = i.getItemMeta();
        if(original.hasDisplayName()){
            im.setDisplayName(original.getDisplayName());
        }
        i.setItemMeta(im);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPacketSending(final PacketEvent e) {
        final PacketContainer packet = e.getPacket();
        if(!packet.hasMetadata("parse")){ //First we check if the packet validator has validated this packet to be parsed by us
            return;
        }
        final boolean usesBaseComponents = (boolean)packet.getMetadata("base-component"); //The packet validator should also tell if this packet uses base components
        e.setCancelled(true); //We cancel the packet as we're going to resend it anyways (ignoring listeners this time)
        Bukkit.getScheduler().runTaskAsynchronously(ChatItem.getInstance(), () -> {
            String json = (String)packet.getMetadata("json"); //The packet validator got the json for us, so no need to get it again
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
            if(name==null){ //something went really bad, so we run away and hide (AKA the player left or is on another server)
                return;
            }

            Player p = Bukkit.getPlayer(name);
            StringBuilder builder = new StringBuilder(json);
            builder.replace(topIndex-(name.length()+6), topIndex, ""); //we remove both the name and the separator from the json string
            json = builder.toString();

            String message = null;
            try {
                if(!p.getItemInHand().getType().equals(Material.AIR)) {
                    ItemStack copy = p.getItemInHand().clone();
                    if(copy.getType().equals(Material.BOOK_AND_QUILL) || copy.getType().equals(Material.WRITTEN_BOOK)){ //filtering written books
                        BookMeta bm = (BookMeta)copy.getItemMeta();
                        bm.setPages(Collections.emptyList());
                        copy.setItemMeta(bm);
                    } else {
                        if (ChatItem.supportsShulkerBoxes()) { //filtering shulker boxes
                            if (SHULKER_BOXES.contains(copy.getType())) {
                                if (copy.hasItemMeta()) {
                                    BlockStateMeta bsm = (BlockStateMeta) copy.getItemMeta();
                                    if (bsm.hasBlockState()) {
                                        ShulkerBox sb = (ShulkerBox) bsm.getBlockState();
                                        for (ItemStack item : sb.getInventory()) {
                                            stripData(item);
                                        }
                                        bsm.setBlockState(sb);
                                    }
                                    copy.setItemMeta(bsm);
                                }
                            }
                        }
                    }
                    message = ChatItem.getManipulator().parse(json, c.PLACEHOLDERS, copy, styleItem(copy, c), ProtocolVersion.getClientVersion(e.getPlayer()));
                } else {
                    if(!c.HAND_DISABLED){
                        message = ChatItem.getManipulator().parseEmpty(json, c.PLACEHOLDERS, c.HAND_NAME, c.HAND_TOOLTIP, p);
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchFieldException | NoSuchMethodException e1) {
                e1.printStackTrace();
            }
            if(message!=null) {
                if(!usesBaseComponents) {
                    packet.getChatComponents().writeSafely(0, WrappedChatComponent.fromJson(message));
                }else{
                    packet.getSpecificModifier(BaseComponent[].class).writeSafely(0, ComponentSerializer.parse(message));
                }
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(e.getPlayer(), packet, false);
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            } else {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(e.getPlayer(), packet, false);
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    public void setStorage(Storage st) {
        c = st;
    }

    public static String styleItem(ItemStack item, Storage c){

        String replacer = c.NAME_FORMAT;
        String amount = c.AMOUNT_FORMAT;
        boolean dname = false;
        if (!c.COLOR_IF_ALREADY_COLORED && item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName()) {
                replacer = ChatColor.stripColor(replacer);
                dname = true;
            }
        } else {
            if (item.hasItemMeta()) {
                if (item.getItemMeta().hasDisplayName()) {
                    dname = true;
                }
            }
        }
        if (item.getAmount() == 1) {
            if (c.FORCE_ADD_AMOUNT) {
                amount = amount.replace(TIMES, "1");
                replacer = replacer.replace(AMOUNT, amount);
            } else {
                replacer = replacer.replace(AMOUNT, "");
            }
        } else {
            amount = amount.replace(TIMES, String.valueOf(item.getAmount()));
            replacer = replacer.replace(AMOUNT, amount);
        }
        if (dname) {
            String trp = item.getItemMeta().getDisplayName();
            if(c.COLOR_IF_ALREADY_COLORED){
                replacer = replacer.replace(NAME, ChatColor.stripColor(trp));
            } else {
                replacer = replacer.replace(NAME, trp);
            }
        } else {
            HashMap<Short, String> translationSection = c.TRANSLATIONS.get(item.getType().name());
            if(translationSection==null){
                String trp = materialToName(item.getType());
                replacer = replacer.replace(NAME, trp);
            }else {
                String translated = translationSection.get(item.getDurability());
                if (translated != null) {
                    replacer = replacer.replace(NAME, translated);
                } else {
                    replacer = replacer.replace(NAME, materialToName(item.getType()));
                }
            }
        }

        return replacer;
    }

}
