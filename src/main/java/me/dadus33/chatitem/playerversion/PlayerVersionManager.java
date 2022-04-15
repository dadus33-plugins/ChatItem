package me.dadus33.chatitem.playerversion;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.playerversion.hooks.DefaultVersionHook;
import me.dadus33.chatitem.playerversion.hooks.ProtocolSupportHook;
import me.dadus33.chatitem.playerversion.hooks.ViaVersionHook;
import org.bukkit.Bukkit;

public class PlayerVersionManager {

    private static final IPlayerVersion playerVersionAdapter;

    static {
        ChatItem pl = ChatItem.getInstance();
        if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
            playerVersionAdapter = new ViaVersionHook();
            pl.getLogger().info("Loading ViaVersion support ...");
        } else if (Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null) {
            playerVersionAdapter = new ProtocolSupportHook();
            pl.getLogger().info("Loading ProtocolSupport support ...");
        } else {
            playerVersionAdapter = new DefaultVersionHook();
        }
    }

    public static IPlayerVersion getPlayerVersionAdapter() {
        return playerVersionAdapter;
    }
}
