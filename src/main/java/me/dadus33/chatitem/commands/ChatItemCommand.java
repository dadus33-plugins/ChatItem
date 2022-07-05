package me.dadus33.chatitem.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v2.ChatListener;
import me.dadus33.chatitem.listeners.InventoryListener;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.Storage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ChatItemCommand implements CommandExecutor, TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Messages.sendMessage(sender, "only-players");
			return false;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			Messages.sendMessage(p, "chatitem-cmd.help");
		} else if (args[0].equalsIgnoreCase("admin")) {
			InventoryListener.open(p);
		} else if (args[0].equalsIgnoreCase("reload")) {
			ChatItem.reload(sender);
		} else if (args[0].equalsIgnoreCase("show")) {
			Storage c = ChatItem.getInstance().getStorage();
			ItemStack item = ChatManager.getUsableItem(p);
			if(ChatManager.canShowItem(p, item, null)) {
				for(Player all : Bukkit.getOnlinePlayers())
					ChatListener.showItem(p, all, item, c.COMMAND_FORMAT.replace("%name%", p.getName()).replace("%item%", ChatManager.SEPARATOR_STR));
			}
		} else if (args[0].equalsIgnoreCase("link") || args[0].equalsIgnoreCase("links")) {
			ConfigurationSection config = ChatItem.getInstance().getConfig()
					.getConfigurationSection("messages.chatitem-cmd.links");
			TextComponent text = new TextComponent(Storage.color(config.getString("begin")));
			for (String key : config.getConfigurationSection("list").getKeys(false)) {
				ConfigurationSection linkConfig = config.getConfigurationSection("list." + key);
				TextComponent linkComp = new TextComponent(Storage.color(linkConfig.getString("message")));
				String hover = Storage.color(linkConfig.getString("hover"));
				if (hover != null)
					linkComp.setHoverEvent(
							new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(hover).create())));
				String link = linkConfig.getString("link");
				if (link != null)
					linkComp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
				text.addExtra(linkComp);
			}
			p.spigot().sendMessage(text);
		} else {
			Messages.sendMessage(p, "chatitem-cmd.help");
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arg) {
		List<String> list = new ArrayList<>();
		String prefix = arg[arg.length - 1].toLowerCase(Locale.ROOT);
		for (String s : Arrays.asList("help", "admin", "reload", "link"))
			if (prefix.isEmpty() || s.startsWith(prefix))
				list.add(s);
		return list.isEmpty() ? null : list;
	}
}
