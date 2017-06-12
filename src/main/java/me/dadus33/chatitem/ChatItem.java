package me.dadus33.chatitem;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import me.dadus33.chatitem.commands.CIReload;
import me.dadus33.chatitem.filters.Log4jFilter;
import me.dadus33.chatitem.json.JSONManipulator;
import me.dadus33.chatitem.json.JSONManipulatorCurrent;
import me.dadus33.chatitem.listeners.ChatEventListener;
import me.dadus33.chatitem.listeners.ChatPacketListener;
import me.dadus33.chatitem.listeners.ChatPacketValidator;
import me.dadus33.chatitem.listeners.HandshakeListener;
import me.dadus33.chatitem.utils.ProtocolSupportUtil;
import me.dadus33.chatitem.utils.Storage;
import org.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatItem extends JavaPlugin {

    public final static int CFG_VER = 11;
    private static ChatItem instance;
    private ChatEventListener chatEventListener;
    private Log4jFilter filter;
    private Storage storage;
    private ProtocolManager pm;
    private ChatPacketListener packetListener;
    private ChatPacketValidator packetValidator;
    private static Class chatMessageTypeClass;
    private static boolean post17 = false;
    private static boolean post111 = false;
    private static boolean post112 = false;
    private static boolean baseComponentAvailable = true;
    private static boolean viaVersion = false;
    private static boolean protocolSupport = false;


    public static void reload(CommandSender sender) {
        ChatItem obj = getInstance();
        obj.pm = ProtocolLibrary.getProtocolManager();
        obj.saveDefaultConfig();
        obj.reloadConfig();
        obj.storage = new Storage(obj.getConfig());
        obj.packetListener.setStorage(obj.storage);
        obj.packetValidator.setStorage(obj.storage);
        obj.chatEventListener.setStorage(obj.storage);
        obj.filter.setStorage(obj.storage);
        if (!obj.storage.RELOAD_MESSAGE.isEmpty())
            sender.sendMessage(obj.storage.RELOAD_MESSAGE);
    }

    public static ChatItem getInstance() {
        return instance;
    }

    public void onEnable() {
        //Save the instance (we're basically a singleton)
        instance = this;

        //Load ProtocolManager
        pm = ProtocolLibrary.getProtocolManager();

        //Load config
        saveDefaultConfig();
        storage = new Storage(getConfig());

        if(isMc18OrLater()) {
            post17 = true; //for actionbar messages ignoring
        }
        if(isMc111OrLater()){
            post111 = true; //for shulker box filtering
        }
        if(isMc112Orlater()){
            post112 = true; //for new ChatType enum instead of using bytes
            try{
                chatMessageTypeClass = Class.forName("net.minecraft.server." + getVersion(Bukkit.getServer()) + ".ChatMessageType");
            } catch (ClassNotFoundException e){
                e.printStackTrace(); //This should never happen anyways, so no need to think of fancy stuff like disabling the plugin
            }
        }

        //Packet listeners
        packetListener = new ChatPacketListener(this, ListenerPriority.LOW, storage, PacketType.Play.Server.CHAT);
        packetValidator = new ChatPacketValidator(this, ListenerPriority.LOWEST, storage, PacketType.Play.Server.CHAT);
        pm.addPacketListener(packetValidator);
        pm.addPacketListener(packetListener);

        if(Bukkit.getPluginManager().getPlugin("ViaVersion") != null){
            viaVersion = true;
        }else if(Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null){
            protocolSupport = true;
            ProtocolSupportUtil.initialize();
        }

        if(!protocolSupport && !viaVersion) {
            //We only implement our own way of getting protocol versions if we have no other choice
            pm.addPacketListener(new HandshakeListener(this, ListenerPriority.LOWEST, PacketType.Handshake.Client.SET_PROTOCOL));
        }

        //Commands
        CIReload rld = new CIReload();
        Bukkit.getPluginCommand("cireload").setExecutor(rld);

        //Bukkit API listeners
        chatEventListener = new ChatEventListener(storage);
        Bukkit.getPluginManager().registerEvents(chatEventListener, this);

        //Check for existence of BaseComponent class (only on spigot)
        try {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
        } catch (ClassNotFoundException e) {
            baseComponentAvailable = false;
        }

        //Initialize Log4J filter (remove ugly console messages)
        filter = new Log4jFilter(storage);

        new Metrics(this);
    }


    public void onDisable() {
        instance = null;
        post17 = false;
    }

    private boolean isMc18OrLater(){
        switch(getVersion(Bukkit.getServer())){
            case "v1_8_R1": return true;
            case "v1_8_R2": return true;
            case "v1_8_R3": return true;
            case "v1_9_R1": return true;
            case "v1_9_R2": return true;
            case "v1_10_R1": return true;
            case "v1_10_R2": return true;
            case "v1_11_R1": return true;
            case "v1_12_R1": return true;
            default: return false;
        }
    }

    private boolean isMc111OrLater(){
        switch(getVersion(Bukkit.getServer())){
            case "v1_11_R1": return true;
            case "v1_12_R1": return true;
            default: return false;
        }
    }

    private boolean isMc112Orlater(){
        switch(getVersion(Bukkit.getServer())){
            case "v1_12_R1": return true;
            default: return false;
        }
    }


    public static String getVersion(Server server) {
        final String packageName = server.getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static boolean supportsActionBar(){
        return post17;
    }

    public static boolean supportsShulkerBoxes(){
        return post111;
    }

    public static boolean supportsChatComponentApi(){
        return baseComponentAvailable;
    }

    public static boolean supportsChatTypeEnum(){
        return post112;
    }

    public static JSONManipulator getManipulator(){
        return new JSONManipulatorCurrent();
    }

    public static boolean usesViaVersion(){
        return viaVersion;
    }

    public static boolean usesProtocolSupport(){
        return protocolSupport;
    }

    public static Class getChatMessageTypeClass(){
        return chatMessageTypeClass;
    }

    public static ChatItem instance(){
        return instance;
    }

}
