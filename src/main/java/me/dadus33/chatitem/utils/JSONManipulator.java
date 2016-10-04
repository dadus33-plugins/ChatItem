package me.dadus33.chatitem.utils;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;


public class JSONManipulator {

    private static Class<?> craftItemStackClass = Reflect.getOBCClass("inventory.CraftItemStack");
    private static Class<?> nmsItemStackClass = Reflect.getNMSClass("ItemStack");
    private static Method asNMSCopy = Reflect.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    private static Class<?> nbtTagCompoundClass = Reflect.getNMSClass("NBTTagCompound");
    private static Method saveNmsItemStackMethod = Reflect.getMethod(nmsItemStackClass, "save", nbtTagCompoundClass);


    public static String parse(String json, String[] replacements, ItemStack item, String repl) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        JsonArray array = obj.getAsJsonArray("extra");
        if (array == null) {
            return null;
        }
        String regex = "";
        for (int i = 0; i < replacements.length; ++i) {
            if (replacements.length == 1) {
                regex = Pattern.quote(replacements[0]);
                break;
            }
            if (i == 0 || i + 1 == replacements.length) {
                if (i == 0) {
                    regex = "(" + Pattern.quote(replacements[i]);
                } else {
                    regex = regex.concat("|").concat(Pattern.quote(replacements[i])).concat(")");
                }
                continue;
            }
            regex = regex.concat("|").concat(Pattern.quote(replacements[i]));
        }
        JsonArray rep = new JsonArray();
        JsonArray use = parser.parse(Translator.toJSON(repl)).getAsJsonArray();

        JsonObject hover = parser.parse("{\"action\":\"show_item\", \"value\": \"\"}").getAsJsonObject();
        Object nmsStack = asNMSCopy.invoke(null, item);
        Object tag = nbtTagCompoundClass.newInstance();
        tag = saveNmsItemStackMethod.invoke(nmsStack, tag);
        String jsonRep = tag.toString();
        hover.addProperty("value", jsonRep);
        for (JsonElement ob : use)
            ob.getAsJsonObject().add("hoverEvent", hover);

        for (int i = 0; i < array.size(); ++i) {
            JsonObject o = array.get(i).getAsJsonObject();
            JsonElement text = o.get("text");
            if (text == null) {
                continue;
            }
            String msg = text.getAsString();
            boolean isLast = false;
            boolean done = false;
            boolean fnd = false;
            String[] splits;
            for (String repls : replacements) {
                if (done) {
                    break;
                }
                isLast = msg.endsWith(repls);
                if (isLast) {
                    done = true;
                    msg = msg.concat(".");
                }
            }
            splits = msg.split(regex);
            fnd = splits.length != 1;
            if (fnd)
                for (int j = 0; j < splits.length; ++j) {
                    boolean endDot = (j == splits.length - 1) && isLast;
                    if (!splits[j].isEmpty() && !endDot) {
                        JsonObject fix = parser.parse(o.toString()).getAsJsonObject();
                        fix.addProperty("text", splits[j]);
                        rep.add(fix);
                    }
                    if (j != splits.length - 1) {
                        rep.addAll(use);
                    }
                }
            if (!fnd) {
                rep.add(o);
            }

        }
        obj.add("extra", rep);
        return obj.toString();
    }


}
