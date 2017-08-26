package me.dadus33.chatitem.listeners;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.ProtocolVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class HandshakeListener extends PacketAdapter{

    public HandshakeListener(Plugin plugin, ListenerPriority listenerPriority, PacketType... types) {
        super(plugin, listenerPriority, types);
    }



    @Override
    public void onPacketReceiving(final PacketEvent e){
        PacketType.Protocol p = e.getPacket().getProtocols().read(0);
        if(p == PacketType.Protocol.STATUS || p == PacketType.Protocol.LEGACY){
            return;
        }
        final int version = e.getPacket().getIntegers().readSafely(0);
        //Delay the mapping to make sure the true address of the player was received when using bungeecord or other types of proxies
        Bukkit.getScheduler().scheduleSyncDelayedTask(ChatItem.getInstance(), () ->
                ProtocolVersion.getPlayerVersionMap().put(ProtocolVersion.stringifyAdress(e.getPlayer().getAddress()), version), 10L);

    }
}
