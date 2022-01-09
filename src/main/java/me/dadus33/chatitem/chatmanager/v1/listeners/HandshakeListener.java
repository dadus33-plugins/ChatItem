package me.dadus33.chatitem.chatmanager.v1.listeners;

import org.bukkit.Bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.DefaultVersionHook;
import me.dadus33.chatitem.utils.Version;

public class HandshakeListener extends PacketAdapter {

    public HandshakeListener(ChatItem pl) {
        super(pl, ListenerPriority.MONITOR, PacketType.Handshake.Client.SET_PROTOCOL);
    }

    @Override
    public void onPacketReceiving(final PacketEvent e){
        PacketType.Protocol p = e.getPacket().getProtocols().read(0);
        if(p == PacketType.Protocol.STATUS || p == PacketType.Protocol.LEGACY){
            return;
        }
        final int version = e.getPacket().getIntegers().readSafely(0);
        //Delay the mapping to make sure the true address of the player was received when using bungeecord or other types of proxies
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () ->
        DefaultVersionHook.PROTOCOL_PER_ADDRESS.put(Version.stringifyAdress(e.getPlayer().getAddress()), version), 10L);
    }
}
