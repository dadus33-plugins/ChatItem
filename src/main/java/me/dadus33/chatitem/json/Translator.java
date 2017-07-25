package me.dadus33.chatitem.json;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;

//Based on DarkSeraphim's system, but using Gson
public class Translator {

    private static final String STYLES = "klmnor"; //All style codes

    public static JsonArray toJson(String old){
        JsonArray message = new JsonArray();
        String[] parts = old.split(Character.toString(ChatColor.COLOR_CHAR));
        JsonObject next = null; //refers to the object we created before (in time) but next in the message, as we're going from end to start
        for(int i = parts.length-1; i >= 0; --i){ //We go in reverse order
            String part = parts[i];
            if(part == null){
                continue;
            }
            if(part.isEmpty()){
                continue;
            }

            char code = Character.toLowerCase(part.charAt(0));

            if(!isColorOrStyle(code)){
                if(next != null){
                    String text = next.get("text").getAsString();
                    text = ChatColor.COLOR_CHAR + part + text;
                    next.addProperty("text", text);
                }else{
                    JsonObject added = new JsonObject();
                    added.addProperty("text", ChatColor.COLOR_CHAR + part);
                    message.add(added);
                    next = added;
                }
                continue;
            }

            if(part.length() == 1){ //If it's just format and no text, we try to format the next element, if it wasn't formatted already (and it's not null)
                if(next == null){
                    continue;
                }

                if(isStyle(code)){
                    if(isAlreadyFormatted(next)){
                        continue;
                    }
                    next.addProperty(getStyleName(code), true);
                    next.addProperty("formatted", true); //mark it as formatted - before returning the JsonArray we'll remove the marking
                }else{ //it's a color
                    if(isAlreadyColored(next)){
                        continue;
                    }
                    next.addProperty("color", getColorName(code));
                }
                continue;
            }
            //Last possibility that remains is that we have a normal color/format + text situation
            JsonObject added = new JsonObject();
            added.addProperty("text", part.substring(1));
            if(isStyle(code)){
                added.addProperty(getStyleName(code), true);
                added.addProperty("formatted", true);
                message.add(added);
                next = added;
                continue;
            }
            //else it can only be a color
            added.addProperty("color", getColorName(code));
            message.add(added);
            next = added;
        }

        int i = message.size()-1;
        JsonArray orderedMessage = new JsonArray();//This is where we'll store the reverted message (in proper order for being sent to the client)
        //First we have to copy all elements
        for(JsonElement el : message){
            orderedMessage.add(el);
        }
        for(JsonElement element : message){
            JsonObject obj = (JsonObject) element;
            obj.remove("formatted"); //We remove any possible occurrences of the 'formatted' flag
            orderedMessage.set(i, obj); //And then we add the object to the properly ordered array
            --i;
        }

        return orderedMessage;
    }

    private static String getColorName(char code){
        return ChatColor.getByChar(code).name().toLowerCase();
    }

    private static String getStyleName(char code){
        switch(code){
            case 'k': return "obfuscated";
            case 'l': return "bold";
            case 'm': return "strikethrough";
            case 'n': return "underlined";
            case 'o': return "italic";
            case 'r': return "reset";
            default: return null; //Should never happen. Made it return null to throw errors if a new format pops up and it really happens
        }
    }

    private static boolean isColorOrStyle(char code){
        return ChatColor.getByChar(code) != null;
    }

    private static boolean isStyle(char c){
        return STYLES.indexOf(c) != -1;
    }

    private static boolean isAlreadyFormatted(JsonObject obj){
        return obj.has("formatted");
    }

    private static boolean isAlreadyColored(JsonObject obj){
        return obj.has("color");
    }



    /*private final StringBuilder BUILDER = new StringBuilder();
    private final StringBuilder STYLE = new StringBuilder();

    String toJSON(String message) {
        if (message == null || message.isEmpty())
            return null;
        String[] parts = message.split(Character.toString(ChatColor.COLOR_CHAR));
        boolean first = true;
        String colour = null;
        String format = null;
        BUILDER.setLength(0);
        BUILDER.append("[");
        boolean ignoreFirst = !parts[0].isEmpty() && ChatColor.getByChar(parts[0].charAt(0)) != null;
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            String newStyle = null;
            if (!ignoreFirst) {
                newStyle = getStyle(part.charAt(0));
            } else {
                ignoreFirst = false;
            }

            if (newStyle != null) {
                part = part.substring(1);
                if (newStyle.startsWith("\"c"))
                    colour = newStyle;
                else
                    format = newStyle;
            }
            if (!part.isEmpty()) {
                if (first)
                    first = false;
                else {
                    BUILDER.append(", ");
                }
                BUILDER.append("{");
                if (colour != null) {
                    BUILDER.append(colour);
                    colour = null;
                }
                if (format != null) {
                    BUILDER.append(format);
                    format = null;
                }
                BUILDER.append(String.format("\"text\":\"%s\"", part));
                BUILDER.append("}");
            }
        }
        BUILDER.append("]");
        return BUILDER.toString();
    }

    private String getStyle(char colour) {
        if (STYLE.length() > 0)
            STYLE.delete(0, STYLE.length());
        switch (colour) {
            case 'k':
                return "\"obfuscated\": true,";
            case 'l':
                return "\"bold\": true,";
            case 'm':
                return "\"strikethrough\": true,";
            case 'n':
                return "\"underlined\": true,";
            case 'o':
                return "\"italic\": true,";
            case 'r':
                return "\"reset\": true,";
            default:
                break;
        }
        ChatColor cc = ChatColor.getByChar(colour);
        if (cc == null)
            return null;
        return STYLE.append("\"color\":\"").append(cc.name().toLowerCase()).append("\", ").toString();
    }*/

}
