package me.dadus33.chatitem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.majek.hexnicks.Nicks;
import me.dadus33.chatitem.ChatItem;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;


public class CIReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if(args.length == 1 && args[0].equalsIgnoreCase("debug") && sender instanceof Player) {
    		Player p = ((Player) sender);
    		
    		TextComponent name = new TextComponent(BukkitComponentSerializer.legacy().toBuilder().hexColors()
    				.useUnusualXRepeatedCharacterHexFormat().build().serialize(Nicks.api().getStoredNick(p).join()));
    		p.spigot().sendMessage(name);
    	} else
    		ChatItem.reload(sender);
        return false;
    }
}
