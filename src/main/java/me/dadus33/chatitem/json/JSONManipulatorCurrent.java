package me.dadus33.chatitem.json;


import com.google.gson.*;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.Reflect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JSONManipulatorCurrent implements JSONManipulator{

    private static final Class<?> CRAFT_ITEM_STACK_CLASS = Reflect.getOBCClass("inventory.CraftItemStack");
    private static final Class<?> NMS_ITEM_STACK_CLASS = Reflect.getNMSClass("ItemStack");
    private static final Method AS_NMS_COPY = Reflect.getMethod(CRAFT_ITEM_STACK_CLASS, "asNMSCopy", ItemStack.class);
    private static final Class<?> NBT_TAG_COMPOUND = Reflect.getNMSClass("NBTTagCompound");
    private static final Method SAVE_NMS_ITEM_STACK_METHOD = Reflect.getMethod(NMS_ITEM_STACK_CLASS, "save", NBT_TAG_COMPOUND);
    private static final Field MAP = Reflect.getField(NBT_TAG_COMPOUND, "map");
    //Tags to be ignored. Currently it only contains tags from PortableHorses, but feel free to submit a pull request to add tags from your plugins
    private static final List<String> IGNORED = Arrays.asList("horsetag", "phorse", "iscnameviz", "cname");

    private static final ConcurrentHashMap<ItemStack, JsonArray> STACKS = new ConcurrentHashMap<>();


    private List<String> replaces;
    private String rgx;
    private JsonArray itemTooltip;
    private JsonArray classicTooltip;
    private final JsonParser PARSER = new JsonParser();
    private final Translator TRANSLATOR = new Translator();


    public String parse(String json, List<String> replacements, final ItemStack item, String replacement) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        JsonObject obj = PARSER.parse(json).getAsJsonObject();
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

        if ((itemTooltip = STACKS.get(item)) == null) {
            JsonArray use = PARSER.parse(TRANSLATOR.toJSON(escapeSpecials(replacement))).getAsJsonArray();
            JsonObject hover = PARSER.parse("{\"action\":\"show_item\", \"value\": \"\"}").getAsJsonObject();
            String jsonRep = stringifyItem(item);
            hover.addProperty("value", jsonRep);
            for (JsonElement ob : use)
                ob.getAsJsonObject().add("hoverEvent", hover);

            itemTooltip = use;
            STACKS.put(item, itemTooltip);
            Bukkit.getScheduler().runTaskLaterAsynchronously(ChatItem.getInstance(), new Runnable() {
                @Override
                public void run() {
                    STACKS.remove(item);
                }
            }, 100L);
        }else{
            System.out.println("FOUND!");
        }

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
                            JsonObject fix = PARSER.parse(st).getAsJsonObject();
                            fix.addProperty("text", splits[j]);
                            rep.add(fix);
                        }
                        if (j != splits.length - 1) {
                            rep.addAll(itemTooltip);
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
                                    rep.addAll(itemTooltip);
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
    public String parseEmpty(String json, List<String> replacements, String repl, List<String> tooltip, Player sender) {
        JsonObject obj = PARSER.parse(json).getAsJsonObject();
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
        JsonArray use = PARSER.parse(TRANSLATOR.toJSON(
                escapeSpecials(
                        repl.replace("{name}", sender.getName()).
                                replace("{display-name}", sender.getDisplayName())))).
                getAsJsonArray();
        JsonObject hover = PARSER.parse("{\"action\":\"show_text\", \"value\": \"\"}").getAsJsonObject();

        StringBuilder oneLineTooltip = new StringBuilder("");
        int index = 0;
        for(String m : tooltip){
           oneLineTooltip.append(m.replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName()));
           ++index;
           if(index!=tooltip.size()-1){
               oneLineTooltip.append('\n');
           }
        }

        hover.add("value", PARSER.parse(TRANSLATOR.toJSON(oneLineTooltip.toString())));
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
                            JsonObject fix = PARSER.parse(st).getAsJsonObject();
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


    private JsonArray parseNoItemArray(JsonArray arr) {
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
                            JsonObject fix = PARSER.parse(st).getAsJsonObject();
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

    private JsonArray parseArray(JsonArray arr) {
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
                            JsonObject fix = PARSER.parse(st).getAsJsonObject();
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


    private String escapeSpecials(String initial){
        return initial.replace("\"", "\\\"").replace("\\", "\\\\").replace("/", "\\/").replace("\b", "\\b")
                .replace("\f", "\\f").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String escapeNonQuotesSpecials(String initial){
        return initial.replace("\\", "\\\\").replace("/", "\\/").replace("\b", "\\b")
                .replace("\f", "\\f").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String stringifyItem(ItemStack item) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        Object nmsStack = AS_NMS_COPY.invoke(null, item);
        Object tag = NBT_TAG_COMPOUND.newInstance();
        tag = SAVE_NMS_ITEM_STACK_METHOD.invoke(nmsStack, tag);
        Map<String, Object> map = (Map<String, Object>) MAP.get(tag);
        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        StringBuilder sb = new StringBuilder("{");
        for(Map.Entry<String, Object> entry : entrySet){
            String key = entry.getKey();
            if(IGNORED.contains(key)){  //We ignore keys that should be ignored
                continue;
            }
            Pattern pattern = Pattern.compile("[{}\\[\\],\":]");
            Matcher matcher = pattern.matcher(key);
            if(matcher.find()){
                continue;
            }
            key = escapeSpecials(key);
            String value = stringifyNBTBase(entry.getValue());
            if(sb.length() > 1){
                sb.append(',');
            }
            sb.append(key).append(':').append(value);
        }
        sb.append("}");
        return sb.toString();
    }

    private String stringifyNBTBase(Object nbtCompound) throws IllegalAccessException {
        if(NBT_TAG_COMPOUND.isInstance(nbtCompound)){ //If we have an NBTTagCompound, not other NBTBase
            Map<String, Object> map = (Map<String, Object>) MAP.get(nbtCompound);
            Set<Map.Entry<String, Object>> entrySet = map.entrySet();
            StringBuilder sb = new StringBuilder("{");
            for(Map.Entry<String, Object> entry : entrySet){
                String key = entry.getKey();
                if(IGNORED.contains(key)){
                    continue;
                }
                Pattern pattern = Pattern.compile("[{}\\[\\],\":]");
                Matcher matcher = pattern.matcher(key);
                if(matcher.find()){
                    continue;
                }
                key = escapeSpecials(key);
                String value = stringifyNBTBase(entry.getValue());
                if(sb.length() > 1){
                    sb.append(',');
                }
                sb.append(key).append(':').append(value);
            }
            sb.append("}");
            return sb.toString();
        }else{
            String toString = nbtCompound.toString();
            if(toString.startsWith("\"") &&toString.endsWith("\"")){
                toString = toString.substring(1, toString.length()-1);
                toString = escapeNonQuotesSpecials(toString);
                StringBuilder bld = new StringBuilder();
                bld.append("\"").append(toString).append("\"");
                return bld.toString();
            }
            return toString;
        }
    }


}
