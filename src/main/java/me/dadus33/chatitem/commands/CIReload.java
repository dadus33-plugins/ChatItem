package me.dadus33.chatitem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.playernamer.PlayerNamerManager;


public class CIReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if(args.length == 1 && args[0].equalsIgnoreCase("debug") && sender instanceof Player) {
    		Player p = ((Player) sender);
    		p.spigot().sendMessage(PlayerNamerManager.getPlayerNamer().getName(p));
    	}
        ChatItem.reload(sender);
        return false;
    }
}
