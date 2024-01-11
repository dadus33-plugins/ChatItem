package me.dadus33.chatitem;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.utils.Utils;

public class Translation {

	private static File folder;
	private static YamlConfiguration legacy, allLangsConfig;
	private static JsonObject messages;
	private static HashMap<String, String> allLangs = new HashMap<>();

	public static void load(ChatItem pl) {
		folder = new File(pl.getDataFolder(), "lang");
		if (!folder.exists())
			folder.mkdir();
		legacy = Utils.copyLoadFile(folder, "legacy.yml", "lang/legacy.yml");
		allLangsConfig = Utils.copyLoadFile(pl.getDataFolder(), "langs.yml", "langs.yml");

		allLangsConfig.getStringList("all").forEach(langKey -> {
			try {
				JsonObject content = JsonParser.parseReader(new InputStreamReader(pl.getResource("lang/" + langKey + ".json"))).getAsJsonObject();
				allLangs.put(langKey, content.get("language.name").getAsString() + " (" + content.get("language.region").getAsString() + ")");
			} catch (Exception e) {
				pl.getLogger().severe("Failed to load lang with key " + langKey + " : " + e.getMessage() + " (" + e.getStackTrace()[0].toString() + ")");
			}
		});
		allLangs = allLangs.entrySet().stream().sorted(Entry.comparingByValue()).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		loadLang(pl.getStorage().language);
	}

	public static HashMap<String, String> getAllLangs() {
		return allLangs;
	}
	
	public static JsonObject getMessages() {
		return messages;
	}
	
	public static void loadLang(String lang) {
		ChatItem pl = ChatItem.getInstance();
		File langFile = new File(folder, lang + ".json");
		if (!langFile.exists()) {
			if (pl.getResource("lang/" + lang + ".json") != null) {
				Utils.copyFile("lang/" + lang + ".json", langFile);
			} else {
				pl.getLogger().severe("Failed to find lang file for " + lang);
				if(lang != "en_gb")
					loadLang("en_gb");
				return;
			}
		}

		try {
			messages = JsonParser.parseReader(new FileReader(langFile)).getAsJsonObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pl.getLogger().info("Loaded translation for " + lang + ".");
	}

	@SuppressWarnings("deprecation")
	public static String get(ItemStack item) {
		String key = (item.getType().isBlock() ? "block" : "item") + ".minecraft." + item.getType().name().toLowerCase();
		if (messages != null && messages.has(key)) {
			return messages.get(key).getAsString();
		} else
			ChatItem.debug("Failed to find translation for " + key);
		return legacy.getString(item.getType().name() + "." + item.getDurability(), item.getType().name().toLowerCase().replace("_", " "));
	}

	protected void thisMethodShouldNotBeUsed_InsteadToUpdateLangFileFromLocalInstall() throws Exception {
		File dir = new File("dir/to/result");
		File folder = new File("C:\\Dir\\To\\AppData\\Roaming\\.minecraft\\assets");// here we are looking for 1.19
		JsonObject json = JsonParser.parseReader(new FileReader(new File(folder, "indexes/1.19.json"))).getAsJsonObject().get("objects").getAsJsonObject();
		for (String key : json.keySet()) {
			if (key.startsWith("minecraft/lang")) { // only lang files
				String lang = key.toString().replace("minecraft/lang/", "");
				String hash = json.get(key).getAsJsonObject().get("hash").getAsString();
				File langFile = new File(folder, "objects/" + hash.substring(0, 2) + "/" + hash);
				JsonObject langs = JsonParser.parseReader(new FileReader(langFile)).getAsJsonObject();
				for (String langKey : new ArrayList<>(langs.keySet())) {
					if (!(langKey.startsWith("block.") || langKey.startsWith("language"))) { // only what we want
						langs.remove(langKey); // removing not needed
					}
				}
				File langDir = new File(dir, lang);
				Files.write(langDir.toPath(), Arrays.asList(langs.toString()));
				System.out.println("Copied " + lang); // using sysout as it's made for basic java, not mc
			}
		}
	}
}
