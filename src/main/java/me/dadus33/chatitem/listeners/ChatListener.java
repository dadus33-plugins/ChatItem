package me.dadus33.chatitem.listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatListener implements Listener {

    private final static String LEFT = "{remaining}";
    private final HashMap<String, Long> COOLDOWNS = new HashMap<>();
	private Storage c;
	
	public ChatListener(Storage c) {
		this.c = c;
	}
	
	public void setStorage(Storage c) {
		this.c = c;
	}

    private String calculateTime(long seconds){
        if(seconds < 60){
            return seconds+c.SECONDS;
        }
        if(seconds < 3600){
            StringBuilder builder = new StringBuilder();
            int minutes = (int) seconds / 60;
            builder.append(minutes).append(c.MINUTES);
            int secs = (int) seconds - minutes*60;
            if(secs != 0){
                builder.append(" ").append(secs).append(c.SECONDS);
            }
            return builder.toString();
        }
        StringBuilder builder = new StringBuilder();
        int hours = (int) seconds / 3600;
        builder.append(hours).append(c.HOURS);
        int minutes = (int) (seconds/60) - (hours*60);
        if(minutes != 0){
            builder.append(" ").append(minutes).append(c.MINUTES);
        }
        int secs = (int) (seconds - ((seconds/60)*60));
        if(secs != 0){
            builder.append(" ").append(secs).append(c.SECONDS);
        }
        return builder.toString();
    }

	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
        if (!p.hasPermission("chatitem.use")) {
            if(!c.LET_MESSAGE_THROUGH) {
                e.setCancelled(true);
            }
            if(!c.NO_PERMISSION_MESSAGE.isEmpty() && c.SHOW_NO_PERM_NORMAL){
                p.sendMessage(c.NO_PERMISSION_MESSAGE);
            }
            return;
        }
        if (p.getItemInHand().getType().equals(Material.AIR)) {
            if (c.DENY_IF_NO_ITEM) {
                e.setCancelled(true);
                if (!c.DENY_MESSAGE.isEmpty())
                    e.getPlayer().sendMessage(c.DENY_MESSAGE);
                return;
            }
            if(c.HAND_DISABLED) {
                return;
            }
        }
        if(c.COOLDOWN > 0 && !p.hasPermission("chatitem.ignore-cooldown")){
            if(COOLDOWNS.containsKey(p.getName())){
                long start = COOLDOWNS.get(p.getName());
                long current = System.currentTimeMillis()/1000;
                long elapsed = current - start;
                if(elapsed >= c.COOLDOWN){
                    COOLDOWNS.remove(p.getName());
                }else{
                    if(!c.LET_MESSAGE_THROUGH) {
                        e.setCancelled(true);
                    }
                    if(!c.COOLDOWN_MESSAGE.isEmpty()){
                        long left = (start + c.COOLDOWN) - current;
                        p.sendMessage(c.COOLDOWN_MESSAGE.replace(LEFT, calculateTime(left)));
                    }
                    return;
                }
            }
        }
        boolean found = false;

        for (String rep : c.PLACEHOLDERS)
            if (e.getMessage().contains(rep)) {
                found = true;
                break;
            }

        if (!found) {
            return;
        }
		e.setCancelled(true);
		String msg = e.getMessage();
		ItemStack item = p.getItemInHand();
		ItemMeta meta = item == null ? null : item.getItemMeta();
		//String[] codeSplitted = p.getDisplayName().split(ChatColor.COLOR_CHAR + "");
		//char code = codeSplitted.length == 0 || !codeSplitted[0].isEmpty() ? codeSplitted[0].charAt(0) : codeSplitted[1].charAt(0);
		TextComponent component = new TextComponent(String.format(e.getFormat(), p.getDisplayName(), ""));
		ChatColor color = ChatColor.getByChar(getColorChat(e.getFormat()));
		for (String args : msg.split(" ")) {
			if (c.PLACEHOLDERS.contains(args)) {
				if(meta != null) {
					//String name = meta.hasDisplayName() ? meta.getDisplayName() : WordUtils.capitalize(item.getType().name().replaceAll("_", " ").toLowerCase());
					//String amountFormat = c.AMOUNT_FORMAT.replace("{times}",  String.valueOf(item.getAmount()));
					//TextComponent itemComponent = new TextComponent(c.NAME_FORMAT.replace("{name}", name).replace("{amount}", amountFormat));
					TextComponent itemComponent = new TextComponent(ChatPacketListenerV2.styleItem(item, c).replaceAll("  ", " "));
					String itemJson = convertItemStackToJson(item);
					itemComponent.setHoverEvent(
							new HoverEvent(Action.SHOW_ITEM, new BaseComponent[] { new TextComponent(itemJson) }));
					//itemComponent.addExtra(ChatColor.RESET + " x" + item.getAmount() + " " + ChatColor.COLOR_CHAR + code);
					component.addExtra(itemComponent);
				} else {
					if(c.HAND_DISABLED)
						component.addExtra(color + args);
					else
						component.addExtra(c.HAND_NAME.replace("{name}", p.getName()).replace("{display-name}", p.getDisplayName()));
				}
			} else {
				component.addExtra(color + args);
			}
			component.addExtra(" ");
			char maybeNextCode = getColorChat(args);
			if(maybeNextCode != 'r') {
				color = ChatColor.getByChar(maybeNextCode);
			}
		}
		Utils.getOnlinePlayers().forEach((pl) -> pl.spigot().sendMessage(component));
	}

	/**
	 * Converts an {@link org.bukkit.inventory.ItemStack} to a Json string for
	 * sending with {@link net.md_5.bungee.api.chat.BaseComponent}'s.
	 *
	 * @param itemStack
	 *            the item to convert
	 * @return the Json string representation of the item
	 */
	public String convertItemStackToJson(ItemStack itemStack) {
		try {
			Class<?> nbtTag = PacketUtils.getNmsClass("NBTTagCompound", "");
			Class<?> craftItemClass = PacketUtils.getObcClass("inventory.CraftItemStack");
			Object nmsNbtTagCompoundObj = nbtTag.newInstance();
			Object nmsItemStackObj = craftItemClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);

			return nmsItemStackObj.getClass().getMethod("save", nbtTag).invoke(nmsItemStackObj, nmsNbtTagCompoundObj).toString();
			
			/*NBTTagCompound nmsNbtTagCompoundObj = new NBTTagCompound();
			net.minecraft.server.v1_8_R3.ItemStack nmsItemStackObj = CraftItemStack.asNMSCopy(itemStack);
			return nmsItemStackObj.save(nmsNbtTagCompoundObj).toString();*/
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the latest color chat
	 * 
	 * @param msg the message that contains color
	 * @return the char of the color or 'r' if nothing found
	 */
	private char getColorChat(String msg) {
		if(msg.length() < 2)
			return 'r';
		for(int i = msg.length() - 2; i > 0; i--) {
			char s = msg.charAt(i);
			if(ChatColor.COLOR_CHAR == s) {
				return msg.charAt(i + 1);
			}
		}
		return 'r';
	}
}
