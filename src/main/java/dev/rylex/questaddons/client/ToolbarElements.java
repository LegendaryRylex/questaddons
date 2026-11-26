package dev.rylex.questaddons.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ToolbarElements {
    private static final String TEXT = "text";
    private static final String COLOR = "color";
    private static final Pattern HEX_COLOR = Pattern.compile("#[0-9A-Fa-f]{6}");
    private static final Map<Character, String> DECORATIONS =
            Map.of('l', "bold", 'o', "italic", 'n', "underlined", 'm', "strikethrough", 'k', "obfuscated");
    private static final String COLOR_NAMES = "black,dark_blue,dark_green,dark_aqua,dark_red,dark_purple,gold,gray,"
            + "dark_gray,blue,green,aqua,red,light_purple,yellow,white";
    private static final String COLOR_CODES = "0123456789abcdef";

    private ToolbarElements() {}

    public static boolean singleLine(String text, int start, int end) {
        return start < end && end <= text.length() && text.substring(start, end).indexOf('\n') < 0;
    }

    public static boolean onComponentLine(String text, int start, int end) {
        if (!singleLine(text, start, end)) {
            return false;
        }
        int lineStart = text.lastIndexOf('\n', start - 1) + 1;
        int newline = text.indexOf('\n', end);
        String line =
                text.substring(lineStart, newline < 0 ? text.length() : newline).trim();
        return ComponentJsonRepair.looksLikeComponent(line);
    }

    public static Optional<String> retext(String element, String selection) {
        String prefix = "{ \"text\": \"" + selection + "\"";
        if (!element.startsWith(prefix)) {
            return Optional.empty();
        }
        String escaped = "{ \"text\": " + new JsonPrimitive(selection) + element.substring(prefix.length());
        try {
            return Optional.of(
                    JsonSpacing.spaced(JsonParser.parseString(escaped).toString()));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public static Optional<String> styled(String text, String formatting) {
        JsonObject element = new JsonObject();
        element.addProperty(TEXT, text);
        if (HEX_COLOR.matcher(formatting).matches()) {
            element.addProperty(COLOR, formatting.toUpperCase());
        } else if (formatting.length() == 1) {
            char code = Character.toLowerCase(formatting.charAt(0));
            int color = COLOR_CODES.indexOf(code);
            if (color >= 0) {
                element.addProperty(COLOR, COLOR_NAMES.split(",")[color]);
            } else if (DECORATIONS.containsKey(code)) {
                element.addProperty(DECORATIONS.get(code), true);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        return Optional.of(JsonSpacing.spaced(element.toString()));
    }
}
