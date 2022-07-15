package me.dadus33.chatitem.chatmanager.v1.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.ColorManager;
import net.md_5.bungee.api.ChatColor;

//Based on DarkSeraphim's system, but using Gson and supporting some more edge cases
public class Translator {

    private static final String STYLES = "klmnor"; //All style codes


    public static JsonArray toJson(String old){
        if(old.lastIndexOf(ChatColor.COLOR_CHAR) == -1){
            JsonArray arr = new JsonArray();
            JsonObject obj = new JsonObject();
            obj.addProperty("text", old);
            arr.add(obj);
            return arr;
        }
        JsonArray message = new JsonArray();
        JsonObject next = new JsonObject();
		String colorCode = "", text = "";
		boolean waiting = false;
		for (char args : old.toCharArray()) {
			if (args == '§') { // begin of color
				if(!text.isEmpty()) {
					next.addProperty("text", text);
					message.add(next);
					next = new JsonObject();
					text = "";
				}
				waiting = true; // waiting for color code
			} else if (waiting) { // if waiting for code and valid str
				// if it's hexademical value and with enough space for full color
				waiting = false;
				if(isStyle(args)) { // is style and not making rich color code
                    next.addProperty(getStyleName(args), true);
					continue;
				}
				if(args == 'x' && !colorCode.isEmpty()) {
					if(!text.isEmpty()) // ignore this if no text before
						text += ColorManager.getColorString(colorCode);
					colorCode = "x";
				} else
					colorCode += args; // a color by itself
			} else {
				waiting = false;
				if(!colorCode.isEmpty()) { // manage color
					if(colorCode.startsWith("x") && colorCode.length() == 7) { // hex color code
						next.addProperty("color", ColorManager.getColor(colorCode).getName());
					} else if(colorCode.length() == 1) { // if only one color code
						if(next.has("color"))
							text += ColorManager.getColor(colorCode);
						else
							next.addProperty("color", ColorManager.getColor(colorCode).getName());
					} else if(!text.isEmpty())// no text before -> color will be used as "color"
						text += ColorManager.getColorString(colorCode);
					
					ChatItem.debug("Add " + colorCode + " to " + text + ", msg: " + message);
					colorCode = "";
				}
				// basic text, not waiting for code after '§'
				text += args;
			}
		}
		if(!text.isEmpty() || message.isEmpty()) {
	        next.addProperty("text", text);
			if(!next.has("color"))
				next.addProperty("color", "white");
			message.add(next);
		}
        return message;
    }

    public static JsonArray toJsonOld(String old){
        if(old.lastIndexOf(ChatColor.COLOR_CHAR) == -1){
            JsonArray arr = new JsonArray();
            JsonObject obj = new JsonObject();
            obj.addProperty("text", old);
            arr.add(obj);
            return arr;
        }
        boolean startsWithCode = old.startsWith(Character.toString(ChatColor.COLOR_CHAR));
        JsonArray message = new JsonArray();
        String[] parts = old.split(Character.toString(ChatColor.COLOR_CHAR));
        JsonObject next = null; //refers to the object we created before (in time) but next in the message, as we're going from end to start
        for(int i = parts.length-1; i >= 0; --i){ //We go in reverse order
            String part = parts[i];

            if(part.isEmpty()){
                continue;
            }

            if(i == 0 && !startsWithCode){
                JsonObject toAdd = new JsonObject();
                toAdd.addProperty("text", part);
                message.add(toAdd);
                break;
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
                    next.addProperty(getStyleName(code), true);
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
                message.add(added);
                next = added;
                continue;
            }
            //else it can only be a color
            added.addProperty("color", getColorName(code));
            //also try to color the next element if not colored already
            if(next != null){
                if(!isAlreadyColored(next)){
                    next.addProperty("color", getColorName(code));
                }
            }
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
            orderedMessage.set(i, obj); //And then we add the object to the properly ordered array
            --i;
        }

        return orderedMessage;
    }

    private static String getColorName(char code){
        return ChatColor.getByChar(code).getName().toLowerCase();
    }

    private static String getStyleName(char code){
        switch(code){
            case 'k': return "obfuscated";
            case 'l': return "bold";
            case 'm': return "strikethrough";
            case 'n': return "underlined";
            case 'o': return "italic";
            case 'r': return "reset";
            default:
            	ChatItem.getInstance().getLogger().severe("Can't find code for style " + code);
            	return null; //Should never happen. Made it return null to throw errors if a new format pops up and it really happens
        }
    }

    private static boolean isColorOrStyle(char code){
        return ChatColor.getByChar(code) != null;
    }

    private static boolean isStyle(char c){
        return STYLES.indexOf(c) != -1;
    }

    private static boolean isAlreadyColored(JsonObject obj){
        return obj.has("color");
    }

}
