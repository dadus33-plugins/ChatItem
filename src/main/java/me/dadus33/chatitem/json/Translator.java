package me.dadus33.chatitem.json;


import org.bukkit.ChatColor;

/*
Many thanks to @DarkSeraphim for this!
I only modified his work to make it compatible with what I was building.
*/
public class Translator {
    private static final StringBuffer BUFFER = new StringBuffer();
    private static final StringBuffer STYLE = new StringBuffer();

    static String toJSON(String message) {
        if (message == null || message.isEmpty())
            return null;
        String[] parts = message.split(Character.toString(ChatColor.COLOR_CHAR));
        boolean first = true;
        String colour = null;
        String format = null;
        BUFFER.setLength(0);
        BUFFER.append("[");
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
                    BUFFER.append(", ");
                }
                BUFFER.append("{");
                if (colour != null) {
                    BUFFER.append(colour);
                    colour = null;
                }
                if (format != null) {
                    BUFFER.append(format);
                    format = null;
                }
                BUFFER.append(String.format("\"text\":\"%s\"", part));
                BUFFER.append("}");
            }
        }
        BUFFER.append("]");
        return BUFFER.toString();
    }

    private static String getStyle(char colour) {
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
    }

}
