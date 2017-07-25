package me.dadus33.chatitem.api;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.json.JSONManipulator;
import me.dadus33.chatitem.json.Translator;
import me.dadus33.chatitem.listeners.ChatPacketListener;
import me.dadus33.chatitem.utils.ProtocolVersion;
import me.dadus33.chatitem.utils.Storage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class APIImplementation implements ChatItemAPI {

    private final static String DEFAULT_JSON = "{\"extra\":[{\"text\":\"i\"}]}";
    private final static String BASE_JSON = "{\"text\":\"\", \"extra\":[]}";
    private final static List<String> DEFAULT_REPLACEMENT = Collections.singletonList("i");

    private Storage c;

    private Logger logger;

    public APIImplementation(Storage st){
        this.c = st;
        this.logger = ChatItem.getInstance().getLogger();
    }

    public void updateLogger(){
        this.logger = ChatItem.getInstance().getLogger();
    }

    public void setStorage(Storage newStorage){
        this.c = newStorage;
    }


    @Override
    public String getJSONFromItem(ItemStack item) {
        JSONManipulator manipulator = ChatItem.getManipulator();
        try {
            return manipulator.parse(DEFAULT_JSON, DEFAULT_REPLACEMENT, item, ChatPacketListener.styleItem(item, c), ProtocolVersion.getServerVersion().MAX_VER);
        } catch (Exception e){
            logger.log(Level.SEVERE, "An unexpected exception was caught while running an API method from ChatItem. Please contact the developer immediately, providing him with the following stack-trace:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getJSONFromItem(ItemStack item, String customReplacement) {
        JSONManipulator manipulator = ChatItem.getManipulator();
        try {
            return manipulator.parse(DEFAULT_JSON, DEFAULT_REPLACEMENT, item, customReplacement, ProtocolVersion.getServerVersion().MAX_VER);
        } catch (Exception e){
            logger.log(Level.SEVERE, "An unexpected exception was caught while running an API method from ChatItem. Please contact the developer immediately, providing him with the following stack-trace:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getJSONFromItem(ItemStack item, Player client) {
        JSONManipulator manipulator = ChatItem.getManipulator();
        try {
            return manipulator.parse(DEFAULT_JSON, DEFAULT_REPLACEMENT, item, ChatPacketListener.styleItem(item, c), ProtocolVersion.getClientVersion(client));
        } catch (Exception e){
            logger.log(Level.SEVERE, "An unexpected exception was caught while running an API method from ChatItem. Please contact the developer immediately, providing him with the following stack-trace:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getJSONFromItem(ItemStack item, String customReplacement, Player client) {
        JSONManipulator manipulator = ChatItem.getManipulator();
        try {
            return manipulator.parse(DEFAULT_JSON, DEFAULT_REPLACEMENT, item, customReplacement, ProtocolVersion.getClientVersion(client));
        } catch (Exception e){
            logger.log(Level.SEVERE, "An unexpected exception was caught while running an API method from ChatItem. Please contact the developer immediately, providing him with the following stack-trace:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getJSONFromInlineItem(String text, ItemStack... items) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(BASE_JSON).getAsJsonObject();
        obj.add("extra", Translator.toJson(text));
        String main = obj.toString();
        int version = ProtocolVersion.getServerVersion().MAX_VER;
        for(int i = 0; i < items.length; ++i){
            JSONManipulator manipulator = ChatItem.getManipulator();
            ItemStack item = items[i];
            String style = ChatPacketListener.styleItem(item, c);
            List<String> replacements = Collections.singletonList("%_"+i);
            try {
                main = manipulator.parse(main, replacements, item, style, version);
            } catch (Exception e){
                logger.log(Level.SEVERE, "An unexpected exception was caught while running an API method from ChatItem. Please contact the developer immediately, providing him with the following stack-trace:");
                e.printStackTrace();
                return null;
            }
        }
        return main;
    }

    @Override
    public String getJSONFromInlineItem(String text, String[] customReplacements, ItemStack... items) {
        JsonObject wrapper = new JsonObject();
        wrapper.add("extra", Translator.toJson(text));
        String main = wrapper.toString();
        int version = ProtocolVersion.getServerVersion().MAX_VER;
        for(int i = 0; i < items.length; ++i){
            JSONManipulator manipulator = ChatItem.getManipulator();
            ItemStack item = items[i];
            String style = Translator.toJson(customReplacements[i]).toString();
            List<String> replacements = Collections.singletonList("%_"+i);
            try {
                main = manipulator.parse(main, replacements, item, style, version);
            } catch (Exception e){
                logger.log(Level.SEVERE, "An unexpected exception was caught while running an API method from ChatItem. Please contact the developer immediately, providing him with the following stack-trace:");
                e.printStackTrace();
                return null;
            }
        }
        return main;
    }

    @Override
    public String getJSONFromInlineItem(String text, Player client, ItemStack... items) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(BASE_JSON).getAsJsonObject();
        obj.add("extra", Translator.toJson(text));
        String main = obj.toString();
        int version = ProtocolVersion.getClientVersion(client);
        for(int i = 0; i < items.length; ++i){
            JSONManipulator manipulator = ChatItem.getManipulator();
            ItemStack item = items[i];
            String style = ChatPacketListener.styleItem(item, c);
            List<String> replacements = Collections.singletonList("%_"+i);
            try {
                main = manipulator.parse(main, replacements, item, style, version);
            } catch (Exception e){
                logger.log(Level.SEVERE, "An unexpected exception was caught while running an API method from ChatItem. Please contact the developer immediately, providing him with the following stack-trace:");
                e.printStackTrace();
                return null;
            }
        }
        return main;
    }

    @Override
    public String getJSONFromInlineItem(String text, Player client, String[] customReplacements, ItemStack... items) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(BASE_JSON).getAsJsonObject();
        obj.add("extra", Translator.toJson(text));
        String main = obj.toString();
        int version = ProtocolVersion.getClientVersion(client);
        for(int i = 0; i < items.length; ++i){
            JSONManipulator manipulator = ChatItem.getManipulator();
            ItemStack item = items[i];
            String style = Translator.toJson(customReplacements[i]).toString();
            List<String> replacements = Collections.singletonList("%_"+i);
            try {
                main = manipulator.parse(main, replacements, item, style, version);
            } catch (Exception e){
                logger.log(Level.SEVERE, "An unexpected exception was caught while running an API method from ChatItem. Please contact the developer immediately, providing him with the following stack-trace:");
                e.printStackTrace();
                return null;
            }
        }
        return main;
    }
}
