package me.dadus33.chatitem.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v2.ChatListener;
import me.dadus33.chatitem.listeners.InventoryListener;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatItemCommand implements CommandExecutor, TabExecutor {

	private static final List<String> orders = Arrays.asList("packet", "chat", "both");
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Messages.sendMessage(sender, "only-players");
			return false;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			Messages.sendMessage(p, "chatitem-cmd.help");
		} else if (args[0].equalsIgnoreCase("admin") && p.hasPermission("chatitem.reload")) {
			InventoryListener.open(p);
		} else if (args[0].equalsIgnoreCase("reload") && p.hasPermission("chatitem.reload")) {
			ChatItem.reload(sender);
		} else if (args[0].equalsIgnoreCase("show") && ChatItem.getInstance().getStorage().cmdShow) {
			Player cible = args.length == 1 ? p : Bukkit.getPlayer(args[1]);
			if(cible == null) {
				Messages.sendMessage(p, "player-not-found", "%arg%", args[1]);
				return false;
			}
			Storage c = ChatItem.getInstance().getStorage();
			ItemStack item = ChatManager.getUsableItem(cible);
			ChatListener.showItem(p, cible, item, c.commandFormat.replace("%name%", cible.getName()).replace("%item%", ChatManager.SEPARATOR + ""));
		} else if (args[0].equalsIgnoreCase("broadcast") && ChatItem.getInstance().getStorage().cmdBroadcast) {
			Player cible = args.length == 1 ? p : Bukkit.getPlayer(args[1]);
			if(cible == null) {
				Messages.sendMessage(p, "player-not-found", "%arg%", args[1]);
				return false;
			}
			Storage c = ChatItem.getInstance().getStorage();
			ItemStack item = ChatManager.getUsableItem(cible);
			for(Player all : Bukkit.getOnlinePlayers())
				ChatListener.showItem(all, cible, item, c.commandFormat.replace("%name%", cible.getName()).replace("%item%", ChatManager.SEPARATOR + ""));
		} else if (args[0].equalsIgnoreCase("link") || args[0].equalsIgnoreCase("links")) {
			ConfigurationSection config = ChatItem.getInstance().getConfig()
					.getConfigurationSection("messages.chatitem-cmd.links");
			TextComponent text = new TextComponent(Storage.color(config.getString("begin")));
			for (String key : config.getConfigurationSection("list").getKeys(false)) {
				ConfigurationSection linkConfig = config.getConfigurationSection("list." + key);
				TextComponent linkComp = new TextComponent(Storage.color(linkConfig.getString("message")));
				String hover = Storage.color(linkConfig.getString("hover"));
				if (hover != null)
					linkComp.setHoverEvent(Utils.createTextHover(hover));
				String link = linkConfig.getString("link");
				if (link != null)
					linkComp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
				text.addExtra(linkComp);
			}
			p.spigot().sendMessage(text);
		} else if (args[0].equalsIgnoreCase("select") && p.hasPermission("chatitem.reload")) {
			if(args.length == 1) {
				p.sendMessage(ChatColor.GRAY + "----------" + ChatColor.GOLD + " ChatItem - Setup " + ChatColor.GRAY + "----------");
				p.sendMessage(ChatColor.AQUA + "Welcome in the help of setup." + ChatColor.YELLOW + " Please follow step by simply answer to test.");
				sendCheckSelectMessage(p, orders.get(0));
			} else {
				String tested = args[1];
				if(!orders.contains(tested)) {
					p.sendMessage(ChatColor.RED + "Unknow test for " + tested + ".");
					return false;
				}
				if(args.length == 2) {
					p.sendMessage(ChatColor.RED + "Can't find if it works");
					return false;
				}
				if(args[2].equalsIgnoreCase("yes")) {
					ChatManager.setTesting(null);
					InventoryListener.setInConfig("manager", tested);
					p.sendMessage(ChatColor.GREEN + "Perfect ! Updating config ...");
					ChatItem.reload(p);
				} else if(args[2].equalsIgnoreCase("no")) {
					int index = orders.indexOf(tested);
					if(orders.size() == (index + 1)) {
						p.sendMessage(ChatColor.RED + "Sad. Sorry but nothing is available. I suggest you to come on discord for more help. Do '/chatitem link' for all links.");
						ChatManager.setTesting(null);
					} else {
						p.sendMessage(ChatColor.RED + "Sad. Checking for next manager ...");
						sendCheckSelectMessage(p, orders.get(index + 1));
					}
				} else {
					p.sendMessage(ChatColor.RED + "Can't find if it works");
				}
			}
			
		} else {
			Messages.sendMessage(p, "chatitem-cmd.help");
		}
		return false;
	}
	
	private void sendCheckSelectMessage(Player p, String testing) {
		ChatManager.setTesting(testing);
		p.chat("Checking for " + testing + ": [i]");

		Bukkit.getScheduler().runTaskLater(ChatItem.getInstance(), () -> {
			TextComponent text = new TextComponent(ChatColor.GOLD + "Did it worked fine? ");
			TextComponent agree = new TextComponent(ChatColor.GREEN + "Yes");
			agree.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatitem select " + testing + " yes"));
			agree.setHoverEvent(Utils.createTextHover(ChatColor.GRAY + "Click to say it worked fine"));
			text.addExtra(agree);
			text.addExtra(" ");
			TextComponent decline = new TextComponent(ChatColor.RED + "No");
			decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chatitem select " + testing + " no"));
			decline.setHoverEvent(Utils.createTextHover(ChatColor.GRAY + "Click to say it's not working as expected"));
			text.addExtra(decline);
			p.spigot().sendMessage(text);
		}, 2);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arg) {
		List<String> list = new ArrayList<>();
		String prefix = arg[arg.length - 1].toLowerCase(Locale.ROOT);
		if(arg.length <= 2) {
			if(sender.hasPermission("chatitem.reload")) {
				for (String s : Arrays.asList("admin", "reload", "select"))
					if (prefix.isEmpty() || s.startsWith(prefix))
						list.add(s);
			}
			if (ChatItem.getInstance().getStorage().cmdShow && (prefix.isEmpty() || "show".startsWith(prefix)))
				list.add("show");
			if (ChatItem.getInstance().getStorage().cmdBroadcast && (prefix.isEmpty() || "broadcast".startsWith(prefix)))
				list.add("broadcast");
			for (String s : Arrays.asList("help", "link"))
				if (prefix.isEmpty() || s.startsWith(prefix))
					list.add(s);
		}
		return list;
	}
}
