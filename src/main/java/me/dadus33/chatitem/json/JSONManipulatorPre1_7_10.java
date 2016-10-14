package me.dadus33.chatitem.json;


import com.google.gson.*;
import me.dadus33.chatitem.utils.Reflect;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;


public class JSONManipulatorPre1_7_10 implements JSONManipulator{

    private static Class<?> craftItemStackClass = Reflect.getOBCClass("inventory.CraftItemStack");
    private static Class<?> nmsItemStackClass = Reflect.getNMSClass("ItemStack");
    private static Method asNMSCopy = Reflect.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    private static Class<?> nbtTagCompoundClass = Reflect.getNMSClass("NBTTagCompound");
    private static Method saveNmsItemStackMethod = Reflect.getMethod(nmsItemStackClass, "save", nbtTagCompoundClass);

    private static String[] replaces;
    private static String rgx;
    private static JsonArray toUse;
    private static JsonParser parser = new JsonParser();


    public String parse(String json, String[] replacements, ItemStack item, String repl) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonObject obj = parser.parse(json).getAsJsonObject();
        JsonArray array = obj.getAsJsonArray("extra");
        replaces = replacements;
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
        rgx = regex;
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

        toUse = use;

        for (int i = 0; i < array.size(); ++i) {
            JsonElement ob = array.get(i);
            boolean inside = false;
            for (String replacement : replacements)
                if (ob.toString().contains(replacement)) {
                    if (inside) {
                        break;
                    }
                    inside = true;
                }
            JsonObject o;
            JsonElement text;
            String msg;
            if(ob.isJsonObject()) {
                o = ob.getAsJsonObject();
                text = o.get("text");
                if (text == null) {
                    JsonElement el = o.get("extra");
                    if (el != null) {
                        JsonArray jar = el.getAsJsonArray();
                        jar = parseArray(jar);
                        o.add("extra", jar);
                    }
                    continue;
                } else {
                    if (text.getAsString().isEmpty()) {
                        JsonElement el = o.get("extra");
                        if (el != null) {
                            JsonArray jar = el.getAsJsonArray();
                            jar = parseArray(jar);
                            o.add("extra", jar);
                        }
                        continue;
                    }
                    msg = text.getAsString();
                }


                boolean isLast = false;
                boolean done = false;
                boolean fnd;
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


            }else{
                if(ob.isJsonArray()){
                    JsonArray jar = ob.getAsJsonArray();
                    jar = parseArray(jar);
                    rep.set(i, jar);
                    continue;
                }else{
                    if(ob.isJsonNull()){
                        continue;
                    }else{
                        msg = ob.getAsString();
                    }
                }



                boolean isLast = false;
                boolean done = false;
                boolean fnd;
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
                            JsonElement fix = new JsonPrimitive(splits[j]);
                            rep.add(fix);
                        }
                        if (j != splits.length - 1) {
                            rep.addAll(use);
                        }
                    }
                if (!fnd) {
                    rep.add(ob);
                }




            }

        }
        obj.add("extra", rep);
        return obj.toString();
    }


    private static JsonArray parseArray(JsonArray arr) {
        JsonArray replacer = new JsonArray();
        for (int i = 0; i < arr.size(); ++i) {
            JsonElement ob = arr.get(i);
            String msg;
            boolean inside = false;
            for (String replacement : replaces)
                if (ob.toString().contains(replacement)) {
                    if (inside) {
                        break;
                    }
                    inside = true;
                }
            if (!inside) {
                continue;
            }


            if(ob.isJsonObject()){
                JsonObject o = ob.getAsJsonObject();
                JsonElement text = o.get("text");
                if (text == null) {
                    JsonElement el = o.get("extra");
                    if (el != null) {
                        JsonArray jar = el.getAsJsonArray();
                        jar = parseArray(jar);
                        o.add("extra", jar);
                    }
                    continue;
                }
                if (text.getAsString().isEmpty()) {
                    JsonElement el = o.get("extra");
                    if (el == null) {
                        continue;
                    }
                    JsonArray jar = el.getAsJsonArray();
                    jar = parseArray(jar);
                    o.add("extra", jar);
                }

                msg = text.getAsString();
                boolean isLast = false;
                boolean done = false;
                boolean fnd;
                String[] splits;
                for (String repls : replaces) {
                    if (done) {
                        break;
                    }
                    isLast = msg.endsWith(repls);
                    if (isLast) {
                        done = true;
                        msg = msg.concat(".");
                    }
                }
                splits = msg.split(rgx);
                fnd = splits.length != 1;
                if (fnd)
                    for (int j = 0; j < splits.length; ++j) {
                        boolean endDot = (j == splits.length - 1) && isLast;
                        if (!splits[j].isEmpty() && !endDot) {
                            JsonObject fix = parser.parse(o.toString()).getAsJsonObject();
                            fix.addProperty("text", splits[j]);
                            replacer.add(fix);
                        }
                        if (j != splits.length - 1) {
                            replacer.addAll(toUse);
                        }
                    }
                if (!fnd) {
                    replacer.add(o);
                }
            }else{
                if(ob.isJsonArray()){
                    JsonArray jar = ob.getAsJsonArray();
                    jar = parseArray(jar);
                    replacer.set(i, jar);
                    continue;
                }else{
                    if(ob.isJsonNull()){
                        continue;
                    }else{
                        msg = ob.getAsString();
                    }
                }
                boolean isLast = false;
                boolean done = false;
                boolean fnd;
                String[] splits;
                for (String repls : replaces) {
                    if (done) {
                        break;
                    }
                    isLast = msg.endsWith(repls);
                    if (isLast) {
                        done = true;
                        msg = msg.concat(".");
                    }
                }
                splits = msg.split(rgx);
                fnd = splits.length != 1;
                if (fnd)
                    for (int j = 0; j < splits.length; ++j) {
                        boolean endDot = (j == splits.length - 1) && isLast;
                        if (!splits[j].isEmpty() && !endDot) {
                            JsonElement fix = new JsonPrimitive(splits[j]);
                            replacer.add(fix);
                        }
                        if (j != splits.length - 1) {
                            replacer.addAll(toUse);
                        }
                    }
                if (!fnd) {
                    replacer.add(ob);
                }
            }



        }
        return replacer;
    }


}
