package me.dadus33.chatitem.commands;

import me.dadus33.chatitem.ChatItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Vlad on 30.09.2016.
 */
public class CIReload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ChatItem.reload(sender);
        return false;
    }
}
