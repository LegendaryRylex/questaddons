package dev.rylex.questaddons.client;

import com.google.gson.JsonPrimitive;
import java.util.function.UnaryOperator;

public final class KeybindInsertion {
    public static final char MARKER = '\uE000';
    private static final String ELEMENT =
            "{\"keybind\": %s, \"color\": \"yellow\", \"hover_event\": {\"action\": \"show_text\", \"value\": %s}}";

    private KeybindInsertion() {}

    public record Result(String text, int cursor) {}

    public static Result insert(String text, int start, int end, String element, UnaryOperator<String> plainToJson) {
        int selectionEnd = text.substring(start, end).contains("\n") ? start : end;
        int lineStart = text.lastIndexOf('\n', start - 1) + 1;
        int lineEnd = text.indexOf('\n', selectionEnd);
        lineEnd = lineEnd < 0 ? text.length() : lineEnd;
        String line = text.substring(lineStart, lineEnd);
        String trimmed = line.trim();

        if (trimmed.isEmpty()) {
            return splice(text, lineStart, lineEnd, element);
        }
        boolean component = ComponentJsonRepair.looksLikeComponent(trimmed);
        if (component && trimmed.startsWith("[") && trimmed.endsWith("]")) {
            Result inserted = intoArray(line, start - lineStart, selectionEnd - lineStart, element);
            return new Result(
                    text.substring(0, lineStart) + inserted.text() + text.substring(lineEnd),
                    lineStart + inserted.cursor());
        }
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return component
                    ? splice(text, lineStart, lineEnd, "[ " + trimmed + ", " + element + " ]")
                    : splice(text, lineEnd, lineEnd, "\n" + element);
        }
        String marked = line.substring(0, start - lineStart) + MARKER + line.substring(selectionEnd - lineStart);
        return splice(text, lineStart, lineEnd, plainToJson.apply(marked));
    }

    public static String element(String keyName, String defaultKey) {
        return ELEMENT.formatted(new JsonPrimitive(keyName), new JsonPrimitive("Default: " + defaultKey));
    }

    private static Result splice(String text, int from, int to, String replacement) {
        return new Result(text.substring(0, from) + replacement + text.substring(to), from + replacement.length());
    }

    private static Result intoArray(String line, int start, int end, String element) {
        int depth = 0;
        int open = -1;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (open >= 0) {
                if (depth == 1 && i >= start) {
                    return splitString(line, open, i, end, element);
                }
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    open = -1;
                }
                continue;
            }
            if (c == '"') {
                open = i;
            } else if (c == '[' || c == '{') {
                depth++;
            } else if (c == ']' || c == '}') {
                if (depth == 1) {
                    return afterElement(line, i, element);
                }
                depth--;
            } else if (c == ',' && depth == 1 && i >= start) {
                return afterElement(line, i, element);
            }
        }
        return afterElement(line, line.lastIndexOf(']'), element);
    }

    private static Result afterElement(String line, int boundary, String element) {
        int end = boundary;
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        String head = line.substring(0, end) + ", " + element;
        return new Result(head + line.substring(end), head.length());
    }

    private static Result splitString(String line, int open, int split, int end, String element) {
        int close = closingQuote(line, open);
        int resume = end > split && end <= close ? end : split;
        if (split == open + 1 && resume == close) {
            return afterElement(line, close + 1, element);
        }
        String head =
                split == open + 1 ? line.substring(0, open) + element : line.substring(0, split) + "\", " + element;
        String tail = resume == close ? line.substring(close + 1) : ", \"" + line.substring(resume);
        return new Result(head + tail, head.length());
    }

    private static int closingQuote(String line, int open) {
        for (int i = open + 1; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '"') {
                return i;
            }
        }
        return line.length();
    }
}
