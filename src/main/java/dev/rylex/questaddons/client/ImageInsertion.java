package dev.rylex.questaddons.client;

import com.google.gson.JsonPrimitive;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.Nullable;

public final class ImageInsertion {
    private ImageInsertion() {}

    public static String tag(String image, int width, int height, @Nullable String align) {
        String tag = "{image:" + image + " width:" + width + " height:" + height;
        return (align == null ? tag : tag + " align:" + align) + "}";
    }

    public static ElementInsertion.Result insert(
            String text,
            int cursor,
            String tag,
            boolean atLineStart,
            String placeholder,
            UnaryOperator<String> fixJson) {
        int lineStart = text.lastIndexOf('\n', cursor - 1) + 1;
        int lineEnd = text.indexOf('\n', cursor);
        lineEnd = lineEnd < 0 ? text.length() : lineEnd;
        String line = text.substring(lineStart, lineEnd);
        String trimmed = line.trim();
        int at = atLineStart ? lineStart : cursor;

        if (trimmed.isEmpty()) {
            String element = atLineStart ? tag + " " + placeholder : tag;
            return splice(text, lineStart, lineEnd, element, element.length());
        }
        if (ComponentJsonRepair.looksLikeComponent(trimmed)) {
            return intoJson(text, lineStart, at, tag, fixJson);
        }
        String element = atLineStart && !Character.isWhitespace(line.charAt(0)) ? tag + " " : tag;
        return splice(text, at, at, element, element.length());
    }

    private static ElementInsertion.Result intoJson(
            String text, int lineStart, int at, String tag, UnaryOperator<String> fixJson) {
        String quoted = new JsonPrimitive(tag).toString();
        ElementInsertion.Result inserted =
                ElementInsertion.insert(text, at, at, "{ \"text\": " + quoted + " }", UnaryOperator.identity());
        String merged = inserted.text();
        int lineEnd = merged.indexOf('\n', lineStart);
        lineEnd = lineEnd < 0 ? merged.length() : lineEnd;
        String line = merged.substring(lineStart, lineEnd);
        String fixed = fixJson.apply(line.trim());
        String indent = line.substring(0, line.indexOf(line.trim()));
        String replacement = indent + fixed;
        int found = replacement.indexOf(quoted);
        int cursor = found < 0 ? replacement.length() : found + quoted.length();
        int close = replacement.indexOf('}', cursor);
        return splice(merged, lineStart, lineEnd, replacement, close < 0 ? cursor : close + 1);
    }

    private static ElementInsertion.Result splice(String text, int from, int to, String replacement, int cursor) {
        return new ElementInsertion.Result(text.substring(0, from) + replacement + text.substring(to), from + cursor);
    }
}
