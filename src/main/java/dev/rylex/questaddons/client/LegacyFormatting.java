package dev.rylex.questaddons.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class LegacyFormatting {
    private static final Map<Character, String> COLORS = Map.ofEntries(
            Map.entry('0', "black"),
            Map.entry('1', "dark_blue"),
            Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"),
            Map.entry('4', "dark_red"),
            Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"),
            Map.entry('7', "gray"),
            Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"),
            Map.entry('a', "green"),
            Map.entry('b', "aqua"),
            Map.entry('c', "red"),
            Map.entry('d', "light_purple"),
            Map.entry('e', "yellow"),
            Map.entry('f', "white"));
    private static final List<Map.Entry<Character, String>> FLAGS = List.of(
            Map.entry('l', "bold"),
            Map.entry('o', "italic"),
            Map.entry('n', "underlined"),
            Map.entry('m', "strikethrough"),
            Map.entry('k', "obfuscated"));
    private static final int HEX_LENGTH = 6;

    private LegacyFormatting() {}

    public static Optional<List<JsonElement>> split(String text) {
        List<JsonElement> parts = new ArrayList<>();
        StringBuilder run = new StringBuilder();
        String color = null;
        Set<Character> flags = new LinkedHashSet<>();
        boolean found = false;
        int i = 0;
        while (i < text.length()) {
            int length = codeLength(text, i);
            if (length == 0) {
                run.append(text.charAt(i));
                i++;
                continue;
            }
            found = true;
            flush(parts, run, color, flags);
            char code = text.charAt(i + 1);
            if (code == '#') {
                color = "#" + text.substring(i + 2, i + 2 + HEX_LENGTH).toUpperCase(Locale.ROOT);
            } else if (code == 'r') {
                color = null;
                flags.clear();
            } else if (COLORS.containsKey(code)) {
                color = COLORS.get(code);
            } else {
                flags.add(code);
            }
            i += length;
        }
        if (!found) {
            return Optional.empty();
        }
        flush(parts, run, color, flags);
        return Optional.of(parts);
    }

    private static int codeLength(String text, int at) {
        char c = text.charAt(at);
        if ((c != '&' && c != '§') || at + 1 >= text.length() || (at > 0 && text.charAt(at - 1) == '\\')) {
            return 0;
        }
        char code = text.charAt(at + 1);
        if (code == '#') {
            int end = at + 2 + HEX_LENGTH;
            return end <= text.length() && text.substring(at + 2, end).matches("[0-9a-fA-F]{6}") ? end - at : 0;
        }
        boolean flag = FLAGS.stream().anyMatch(entry -> entry.getKey() == code);
        return code == 'r' || COLORS.containsKey(code) || flag ? 2 : 0;
    }

    private static void flush(List<JsonElement> parts, StringBuilder run, String color, Set<Character> flags) {
        if (run.isEmpty()) {
            return;
        }
        String text = run.toString();
        run.setLength(0);
        if (color == null && flags.isEmpty()) {
            parts.add(new JsonPrimitive(text));
            return;
        }
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        if (color != null) {
            part.addProperty("color", color);
        }
        FLAGS.stream()
                .filter(entry -> flags.contains(entry.getKey()))
                .forEach(entry -> part.addProperty(entry.getValue(), true));
        parts.add(part);
    }
}
