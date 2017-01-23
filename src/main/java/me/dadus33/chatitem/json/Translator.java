package me.dadus33.chatitem.json;


import org.bukkit.ChatColor;

/*
Many thanks to @DarkSeraphim for this!
I only modified his work to make it compatible with what I was building.
*/
public class Translator {
    private static final StringBuilder BUILDER = new StringBuilder();
    private static final StringBuilder STYLE = new StringBuilder();

    static String toJSON(String message) {
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
