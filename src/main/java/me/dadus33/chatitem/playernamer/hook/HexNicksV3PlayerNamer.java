package me.dadus33.chatitem.playernamer.hook;

import dev.majek.hexnicks.HexNicks;
import org.bukkit.entity.Player;

import me.dadus33.chatitem.playernamer.IPlayerNamer;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

public class HexNicksV3PlayerNamer implements IPlayerNamer {

    @Override
    public BaseComponent[] getName(Player p) {
        return BungeeComponentSerializer.get().serialize(HexNicks.api().getStoredNick(p).join());
    }

}
