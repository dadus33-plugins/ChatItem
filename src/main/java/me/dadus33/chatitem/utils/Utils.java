package me.dadus33.chatitem.utils;

import me.dadus33.chatitem.ChatItem;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class Utils {

    private final static TreeMap<Integer, String> ROMAN_KEYS = new TreeMap<>();
    private final static HashMap<String, String> ENCHANTS_NAMES = new HashMap<>();

    static {
        ROMAN_KEYS.put(1000, "M");
        ROMAN_KEYS.put(900, "CM");
        ROMAN_KEYS.put(500, "D");
        ROMAN_KEYS.put(400, "CD");
        ROMAN_KEYS.put(100, "C");
        ROMAN_KEYS.put(90, "XC");
        ROMAN_KEYS.put(50, "L");
        ROMAN_KEYS.put(40, "XL");
        ROMAN_KEYS.put(10, "X");
        ROMAN_KEYS.put(9, "IX");
        ROMAN_KEYS.put(5, "V");
        ROMAN_KEYS.put(4, "IV");
        ROMAN_KEYS.put(1, "I");

        ENCHANTS_NAMES.put("ARROW_DAMAGE", "Power");
        ENCHANTS_NAMES.put("ARROW_FIRE", "Flame");
        ENCHANTS_NAMES.put("ARROW_INFINITE", "Infinity");
        ENCHANTS_NAMES.put("ARROW_KNOCKBACK", "Punch");
        ENCHANTS_NAMES.put("BINDING_CURSE", "Curse of Binding");
        ENCHANTS_NAMES.put("DAMAGE_ALL", "Sharpness");
        ENCHANTS_NAMES.put("DAMAGE_ARTHROPODS", "Bane of Arthropods");
        ENCHANTS_NAMES.put("DAMAGE_UNDEAD", "Smite");
        ENCHANTS_NAMES.put("DEPTH_STRIDER", "Depth Strider");
        ENCHANTS_NAMES.put("DIG_SPEED", "Efficiency");
        ENCHANTS_NAMES.put("DURABILITY", "Unbreaking");
        ENCHANTS_NAMES.put("FIRE_ASPECT", "Fire Aspect");
        ENCHANTS_NAMES.put("FROST_WALKER", "Frost Walker");
        ENCHANTS_NAMES.put("KNOCKBACK", "Knockback");
        ENCHANTS_NAMES.put("LOOT_BONUS_BLOCKS", "Fortune");
        ENCHANTS_NAMES.put("LOOT_BONUS_MOBS", "Looting");
        ENCHANTS_NAMES.put("LUCK", "Luck of the Sea");
        ENCHANTS_NAMES.put("LURE", "Lure");
        ENCHANTS_NAMES.put("MENDING", "Mending");
        ENCHANTS_NAMES.put("OXYGEN", "Respiration");
        ENCHANTS_NAMES.put("PROTECTION_ENVIRONMENTAL    ", "Protection");
        ENCHANTS_NAMES.put("PROTECTION_EXPLOSIONS", "Blast Protection");
        ENCHANTS_NAMES.put("PROTECTION_FALL", "Feather Falling");
        ENCHANTS_NAMES.put("PROTECTION_FIRE", "Fire Protection");
        ENCHANTS_NAMES.put("PROTECTION_PROJECTILE", "Projectile Protection");
        ENCHANTS_NAMES.put("SILK_TOUCH", "Silk Touch");
        ENCHANTS_NAMES.put("SWEEPING_EDGE", "Sweeping Edge");
        ENCHANTS_NAMES.put("THORNS", "Thorns");
        ENCHANTS_NAMES.put("VANISHING_CURSE", "Cure of Vanishing");
        ENCHANTS_NAMES.put("WATER_WORKER", "Aqua Affinity");
    }

    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public static String toRoman(int number) {
        int l = ROMAN_KEYS.floorKey(number);
        if (number == l) {
            return ROMAN_KEYS.get(number);
        }
        return ROMAN_KEYS.get(l) + toRoman(number - l);
    }

    @SuppressWarnings("deprecation")
    public static String getEnchantName(Enchantment enchant) {
        String name = ENCHANTS_NAMES.get(enchant.getName());
        if (name == null) {

            ChatItem.getInstance().getLogger().warning("Unknown enchant " + enchant.getName() + ". Please report this to Elikill58.");
        }
        return name;
    }

    public static String getFromURL(String urlName) {
        ChatItem pl = ChatItem.getInstance();
        try {
            URL url = new URL(urlName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "ChatItem " + pl.getDescription().getVersion());
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String input;
            while ((input = br.readLine()) != null)
                content.append(input);
            br.close();
            return content.toString();
        } catch (SocketTimeoutException e) {
            pl.getLogger().info("Failed to access to " + urlName + " (Reason: timed out).");
        } catch (UnknownHostException | MalformedURLException e) {
            pl.getLogger().info("Could not use the internet connection to check for update or send stats");
        } catch (ConnectException e) {
            pl.getLogger().warning("Cannot connect to " + urlName + " (Reason: " + e.getMessage() + ").");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
