package me.dadus33.chatitem.json;


import com.google.gson.*;
import me.dadus33.chatitem.namecheck.Checker;
import me.dadus33.chatitem.utils.Reflect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;


public class JSONManipulatorCurrent implements JSONManipulator{

    private static final Class<?> craftItemStackClass = Reflect.getOBCClass("inventory.CraftItemStack");
    private static final Class<?> nmsItemStackClass = Reflect.getNMSClass("ItemStack");
    private static final Method asNMSCopy = Reflect.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    private static final Class<?> nbtTagCompoundClass = Reflect.getNMSClass("NBTTagCompound");
    private static final Method saveNmsItemStackMethod = Reflect.getMethod(nmsItemStackClass, "save", nbtTagCompoundClass);

    private static List<String> replaces;
    private static String rgx;
    private static JsonArray itemTooltip;
    private static JsonArray classicTooltip;
    private static JsonParser parser = new JsonParser();


    public String parse(String json, List<String> replacements, ItemStack item, String replacement) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        JsonObject obj = parser.parse(json).getAsJsonObject();
        JsonArray array = obj.getAsJsonArray("extra");
        replaces = replacements;
        String regex = "";
        for (int i = 0; i < replacements.size(); ++i) {
            if (replacements.size() == 1) {
                regex = Pattern.quote(replacements.get(0));
                break;
            }
            if (i == 0 || i + 1 == replacements.size()) {
                if (i == 0) {
                    regex = "(" + Pattern.quote(replacements.get(i));
                } else {
                    regex = regex.concat("|").concat(Pattern.quote(replacements.get(i))).concat(")");
                }
                continue;
            }
            regex = regex.concat("|").concat(Pattern.quote(replacements.get(i)));
        }
        rgx = regex;
        JsonArray rep = new JsonArray();
        JsonArray use;
        try {
            use = parser.parse(Translator.toJSON(escapeBackslash(replacement))).getAsJsonArray();
        }catch(JsonParseException e){ //in case the name of the item was already escaped
            use = parser.parse(Translator.toJSON(replacement)).getAsJsonArray();
        }
        JsonObject hover = parser.parse("{\"action\":\"show_item\", \"value\": \"\"}").getAsJsonObject();
        Object nmsStack = asNMSCopy.invoke(null, item);
        Object tag = nbtTagCompoundClass.newInstance();
        tag = saveNmsItemStackMethod.invoke(nmsStack, tag);
        String stringed = tag.toString();
        String jsonRep = escapeBackslash(stringed);

        if(!Checker.checkItem(jsonRep)){ //We make sure the item will display properly client side by making the same checks the client does
            jsonRep = stringed;
        }
        hover.addProperty("value", jsonRep);
        for (JsonElement ob : use)
            ob.getAsJsonObject().add("hoverEvent", hover);

        itemTooltip = use;

        for (int i = 0; i < array.size(); ++i) {
            if (array.get(i).isJsonObject()){
                JsonObject o = array.get(i).getAsJsonObject();
                boolean inside = false;
                for (String replace : replacements)
                    if (o.toString().contains(replace)) {
                        if (inside) {
                            break;
                        }
                        inside = true;
                    }
                JsonElement text = o.get("text");
                if (text == null) {
                    JsonElement el = o.get("extra");
                    if (el != null) {
                        JsonArray jar = el.getAsJsonArray();
                        if(jar.size()!=0) {
                            jar = parseArray(jar);
                            o.add("extra", jar);
                        }else{
                            o.remove("extra");
                        }
                    }
                    continue;
                } else {
                    if (text.getAsString().isEmpty()) {
                        JsonElement el = o.get("extra");
                        if (el != null) {
                            JsonArray jar = el.getAsJsonArray();
                            if(jar.size()!=0) {
                                jar = parseArray(jar);
                                o.add("extra", jar);
                            }else{
                                o.remove("extra");
                            }
                        }
                    }
                }

                String msg = text.getAsString();
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
                            String st = o.toString();
                            JsonObject fix = parser.parse(st).getAsJsonObject();
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
                if(array.get(i).isJsonNull()){
                    continue;
                }else{
                    if(array.get(i).isJsonArray()){
                        JsonArray jar = array.get(i).getAsJsonArray();
                        if(jar.size()!=0) {
                            jar = parseArray(array.get(i).getAsJsonArray());
                            rep.set(i, jar);
                        }
                    }else{


                        String msg = array.get(i).getAsString();
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
                            rep.add(array.get(i));
                        }


                    }
                }
            }

        }
        obj.add("extra", rep);
        return obj.toString();
    }

    @Override
    public String parseEmpty(String json, List<String> replacements, String repl, String tooltip, Player sender) {
        JsonObject obj = parser.parse(json).getAsJsonObject();
        JsonArray array = obj.getAsJsonArray("extra");
        replaces = replacements;
        String regex = "";
        for (int i = 0; i < replacements.size(); ++i) {
            if (replacements.size() == 1) {
                regex = Pattern.quote(replacements.get(0));
                break;
            }
            if (i == 0 || i + 1 == replacements.size()) {
                if (i == 0) {
                    regex = "(" + Pattern.quote(replacements.get(i));
                } else {
                    regex = regex.concat("|").concat(Pattern.quote(replacements.get(i))).concat(")");
                }
                continue;
            }
            regex = regex.concat("|").concat(Pattern.quote(replacements.get(i)));
        }
        rgx = regex;
        JsonArray rep = new JsonArray();
        JsonArray use;
        use = parser.parse(Translator.toJSON(
                escapeBackslash(
                        repl.replace("{name}", sender.getName()).
                                replace("{display-name}", sender.getDisplayName())))).
                getAsJsonArray();
        JsonObject hover = parser.parse("{\"action\":\"show_text\", \"value\": \"\"}").getAsJsonObject();
        tooltip = tooltip.replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName());
        hover.add("value", parser.parse(Translator.toJSON(tooltip)));
        for (JsonElement ob : use)
            ob.getAsJsonObject().add("hoverEvent", hover);

        classicTooltip = use;

        for (int i = 0; i < array.size(); ++i) {
            if (array.get(i).isJsonObject()){
                JsonObject o = array.get(i).getAsJsonObject();
                boolean inside = false;
                for (String replace : replacements)
                    if (o.toString().contains(replace)) {
                        if (inside) {
                            break;
                        }
                        inside = true;
                    }
                JsonElement text = o.get("text");
                if (text == null) {
                    JsonElement el = o.get("extra");
                    if (el != null) {
                        JsonArray jar = el.getAsJsonArray();
                        if(jar.size()!=0) {
                            jar = parseNoItemArray(jar);
                            o.add("extra", jar);
                        }else{
                            o.remove("extra");
                        }
                    }
                    continue;
                } else {
                    if (text.getAsString().isEmpty()) {
                        JsonElement el = o.get("extra");
                        if (el != null) {
                            JsonArray jar = el.getAsJsonArray();
                            if(jar.size()!=0) {
                                jar = parseNoItemArray(jar);
                                o.add("extra", jar);
                            }else{
                                o.remove("extra");
                            }
                        }
                    }
                }

                String msg = text.getAsString();
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
                            String st = o.toString();
                            JsonObject fix = parser.parse(st).getAsJsonObject();
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
                if(array.get(i).isJsonNull()){
                    continue;
                }else{
                    if(array.get(i).isJsonArray()){
                        JsonArray jar = array.get(i).getAsJsonArray();
                        if(jar.size()!=0) {
                            jar = parseNoItemArray(array.get(i).getAsJsonArray());
                            rep.set(i, jar);
                        }
                    }else{


                        String msg = array.get(i).getAsString();
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
                            rep.add(array.get(i));
                        }


                    }
                }
            }

        }
        obj.add("extra", rep);
        return obj.toString();
    }


    private static JsonArray parseNoItemArray(JsonArray arr) {
        JsonArray replacer = new JsonArray();
        for (int i = 0; i < arr.size(); ++i) {
            if (arr.get(i).isJsonObject()){
                    JsonObject o = arr.get(i).getAsJsonObject();
                boolean inside = false;
                for (String replacement : replaces)
                    if (o.toString().contains(replacement)) {
                        if (inside) {
                            break;
                        }
                        inside = true;
                    }
                if (!inside) { //the placeholder we're looking for is not inside this element, so we continue searching
                    replacer.add(o);
                    continue;
                }
                JsonElement text = o.get("text");
                if (text == null) {
                    continue;
                }
                if (text.getAsString().isEmpty()) {
                    JsonElement el = o.get("extra");
                    if (el == null) {
                        continue;
                    }
                    JsonArray jar = el.getAsJsonArray();
                    if(jar.size()!=0) {
                        jar = parseNoItemArray(jar);
                        o.add("extra", jar);
                    }else{
                        o.remove("extra");
                    }
                }

                String msg = text.getAsString();
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
                            String st = o.toString();
                            JsonObject fix = parser.parse(st).getAsJsonObject();
                            fix.addProperty("text", splits[j]);
                            replacer.add(fix);
                        }
                        if (j != splits.length - 1) {
                            replacer.addAll(classicTooltip);
                        }
                    }
                if (!fnd) {
                    replacer.add(o);
                }
            }else{
                if(arr.get(i).isJsonNull()){
                    continue;
                }else{
                    if(arr.get(i).isJsonArray()){
                        JsonArray jar = arr.get(i).getAsJsonArray();
                        if(jar.size()!=0) {
                            jar = parseNoItemArray(arr.get(i).getAsJsonArray());
                            replacer.set(i, jar);
                        }
                    }else{
                        String msg = arr.get(i).getAsString();
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
                                    replacer.addAll(classicTooltip);
                                }
                            }
                        if (!fnd) {
                            replacer.add(arr.get(i));
                        }
                    }
                }
            }

        }
        return replacer;
    }

    private static JsonArray parseArray(JsonArray arr) {
        JsonArray replacer = new JsonArray();
        for (int i = 0; i < arr.size(); ++i) {
            if (arr.get(i).isJsonObject()){
                JsonObject o = arr.get(i).getAsJsonObject();
                boolean inside = false;
                for (String replacement : replaces)
                    if (o.toString().contains(replacement)) {
                        if (inside) {
                            break;
                        }
                        inside = true;
                    }
                if (!inside) { //the placeholder we're looking for is not inside this element, so we continue searching
                    replacer.add(o);
                    continue;
                }
                JsonElement text = o.get("text");
                if (text == null) {
                    continue;
                }
                if (text.getAsString().isEmpty()) {
                    JsonElement el = o.get("extra");
                    if (el == null) {
                        continue;
                    }
                    JsonArray jar = el.getAsJsonArray();
                    if(jar.size()!=0) {
                        jar = parseArray(jar);
                        o.add("extra", jar);
                    }else{
                        o.remove("extra");
                    }
                }

                String msg = text.getAsString();
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
                            String st = o.toString();
                            JsonObject fix = parser.parse(st).getAsJsonObject();
                            fix.addProperty("text", splits[j]);
                            replacer.add(fix);
                        }
                        if (j != splits.length - 1) {
                            replacer.addAll(itemTooltip);
                        }
                    }
                if (!fnd) {
                    replacer.add(o);
                }
            }else{
                if(arr.get(i).isJsonNull()){
                    continue;
                }else{
                    if(arr.get(i).isJsonArray()){
                        JsonArray jar = arr.get(i).getAsJsonArray();
                        if(jar.size()!=0) {
                            jar = parseArray(arr.get(i).getAsJsonArray());
                            replacer.set(i, jar);
                        }
                    }else{
                        String msg = arr.get(i).getAsString();
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
                                    replacer.addAll(itemTooltip);
                                }
                            }
                        if (!fnd) {
                            replacer.add(arr.get(i));
                        }
                    }
                }
            }

        }
        return replacer;
    }


    private String escapeBackslash(String json){
        return json.replace("\\",  "\\\\");
    }


}
