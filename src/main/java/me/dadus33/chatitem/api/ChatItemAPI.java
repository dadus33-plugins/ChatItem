package me.dadus33.chatitem.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ChatItemAPI {
    String getJSONFromItem(ItemStack item);

    String getJSONFromItem(ItemStack item, String customReplacement);

    String getJSONFromItem(ItemStack item, Player client);

    String getJSONFromItem(ItemStack item, String customReplacement, Player client);

    String getJSONFromInlineItem(String text, ItemStack... items);

    String getJSONFromInlineItem(String text, String[] customReplacements, ItemStack... items);

    String getJSONFromInlineItem(String text, Player client, ItemStack... items);

    String getJSONFromInlineItem(String text, Player client, String[] customReplacements, ItemStack... items);
}
