package me.dadus33.chatitem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.Storage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatItemCommand implements CommandExecutor {

    @SuppressWarnings("deprecation")
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	Storage c = ChatItem.getInstance().getStorage();
    	if(!(sender instanceof Player)) {
    		sender.sendMessage(c.ONLY_PLAYERS);
    		return false;
    	}
    	Player p = (Player) sender;
    	if(args.length == 0) {
    		c.CI_HELP.forEach(p::sendMessage);
    	} else if(args[0].equalsIgnoreCase("admin")) {
    		// here open inventory
    		p.sendMessage("admin soon");
    	} else if(args[0].equalsIgnoreCase("reload")) {
    		ChatItem.reload(sender);
    	} else if(args[0].equalsIgnoreCase("link") || args[0].equalsIgnoreCase("links")) {
    		ConfigurationSection config = ChatItem.getInstance().getConfig().getConfigurationSection("messages.chatitem-cmd.links");
    		TextComponent text = new TextComponent(Storage.color(config.getString("begin")));
    		for(String key : config.getConfigurationSection("list").getKeys(false)) {
    			ConfigurationSection linkConfig = config.getConfigurationSection("list." + key);
        		TextComponent linkComp = new TextComponent(Storage.color(linkConfig.getString("message")));
        		String hover = Storage.color(linkConfig.getString("hover"));
        		if(hover != null)
        			linkComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        		String link = linkConfig.getString("link"); 
        		if(link != null)
        			linkComp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        		text.addExtra(linkComp);
    		}
    		p.spigot().sendMessage(text);
    	} else {
    		c.CI_HELP.forEach(p::sendMessage);
    	}
        return false;
    }
}
