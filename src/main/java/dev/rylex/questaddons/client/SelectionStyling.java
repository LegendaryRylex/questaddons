package dev.rylex.questaddons.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class SelectionStyling {
    private static final Set<String> CONTENT_KEYS = Set.of("text", "translate", "score", "selector", "keybind", "nbt");
    private static final String TEXT = "text";
    private static final String EXTRA = "extra";

    private SelectionStyling() {}

    private record Span(int from, int to) {
        boolean overlaps(int start, int end) {
            return from < end && to > start;
        }

        boolean holds(int start, int end) {
            return start >= from && end <= to;
        }
    }

    static Optional<ElementInsertion.Result> apply(String line, int start, int end, String element) {
        if (end <= start) {
            return Optional.empty();
        }
        Optional<JsonArray> array = parse(line).filter(JsonElement::isJsonArray).map(JsonElement::getAsJsonArray);
        Optional<JsonElement> inserted = parse(element);
        if (array.isEmpty() || inserted.isEmpty()) {
            return Optional.empty();
        }
        List<Span> spans = elements(line);
        if (spans.size() != array.get().size()) {
            return Optional.empty();
        }
        String selection = line.substring(start, end);
        Optional<JsonObject> wrap = inserted.filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .filter(object -> object.has(TEXT) && isString(object.get(TEXT)))
                .filter(object -> object.get(TEXT).getAsString().equals(selection));

        for (int i = 0; i < spans.size(); i++) {
            Span span = spans.get(i);
            JsonElement current = array.get().get(i);
            if (isString(current) && start > span.from() && end < span.to()) {
                return Optional.empty();
            }
            if (current.isJsonObject()) {
                Optional<Span> value = textValue(line, span);
                if (value.isPresent() && value.get().holds(start, end)) {
                    return split(line, span, value.get(), current.getAsJsonObject(), start, end, wrap, inserted.get());
                }
            }
        }
        if (wrap.isEmpty()) {
            return Optional.empty();
        }
        List<Integer> touched = new ArrayList<>();
        for (int i = 0; i < spans.size(); i++) {
            if (spans.get(i).overlaps(start, end)) {
                touched.add(i);
            }
        }
        if (touched.isEmpty()) {
            return Optional.empty();
        }
        String out = line;
        int cursor = 0;
        for (int k = touched.size() - 1; k >= 0; k--) {
            int i = touched.get(k);
            Span span = spans.get(i);
            String styled = write(styled(object(array.get().get(i)), wrap.get()));
            out = out.substring(0, span.from()) + styled + out.substring(span.to());
            if (k == touched.size() - 1) {
                cursor = span.from() + styled.length();
            } else {
                cursor += styled.length() - (span.to() - span.from());
            }
        }
        return Optional.of(new ElementInsertion.Result(out, cursor));
    }

    private static Optional<ElementInsertion.Result> split(
            String line,
            Span span,
            Span value,
            JsonObject original,
            int start,
            int end,
            Optional<JsonObject> wrap,
            JsonElement inserted) {
        String before = decode(line.substring(value.from(), start));
        String selected = decode(line.substring(start, end));
        String after = decode(line.substring(end, value.to()));
        if (before == null || selected == null || after == null) {
            return Optional.empty();
        }
        List<String> pieces = new ArrayList<>();
        if (!before.isEmpty()) {
            pieces.add(write(withText(original, before, false)));
        }
        JsonElement middle = wrap.isPresent() ? styled(withText(original, selected, false), wrap.get()) : inserted;
        String middleText = write(middle);
        pieces.add(middleText);
        if (!after.isEmpty() || original.has(EXTRA)) {
            pieces.add(write(withText(original, after, true)));
        }
        String head = line.substring(0, span.from());
        String joined = String.join(", ", pieces);
        int cursor = head.length() + joined.indexOf(middleText) + middleText.length();
        return Optional.of(new ElementInsertion.Result(head + joined + line.substring(span.to()), cursor));
    }

    private static JsonObject withText(JsonObject original, String text, boolean keepExtra) {
        JsonObject copy = original.deepCopy();
        copy.addProperty(TEXT, text);
        if (!keepExtra) {
            copy.remove(EXTRA);
        }
        return copy;
    }

    private static JsonObject styled(JsonObject target, JsonObject style) {
        style.entrySet().stream()
                .filter(entry -> !CONTENT_KEYS.contains(entry.getKey()))
                .forEach(entry -> target.add(entry.getKey(), entry.getValue().deepCopy()));
        return target;
    }

    private static JsonObject object(JsonElement element) {
        if (element.isJsonObject()) {
            return element.getAsJsonObject().deepCopy();
        }
        JsonObject object = new JsonObject();
        if (element.isJsonArray()) {
            object.addProperty(TEXT, "");
            object.add(EXTRA, element.deepCopy());
        } else {
            object.addProperty(TEXT, element.getAsString());
        }
        return object;
    }

    private static List<Span> elements(String line) {
        List<Span> spans = new ArrayList<>();
        int depth = 0;
        int from = -1;
        int last = -1;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                int close = closingQuote(line, i);
                if (depth == 1) {
                    spans.add(new Span(from < 0 ? i : from, close + 1));
                    from = -1;
                }
                i = close;
            } else if (c == '[' || c == '{') {
                if (depth == 1 && from < 0) {
                    from = i;
                }
                depth++;
            } else if (c == ']' || c == '}') {
                depth--;
                if (depth == 1) {
                    spans.add(new Span(from, i + 1));
                    from = -1;
                } else if (depth == 0 && from >= 0) {
                    spans.add(new Span(from, last + 1));
                    from = -1;
                }
            } else if (depth == 1 && c == ',') {
                if (from >= 0) {
                    spans.add(new Span(from, last + 1));
                    from = -1;
                }
            } else if (depth == 1 && !Character.isWhitespace(c)) {
                if (from < 0) {
                    from = i;
                }
                last = i;
            }
        }
        return spans;
    }

    private static Optional<Span> textValue(String line, Span object) {
        int depth = 0;
        for (int i = object.from() + 1; i < object.to() - 1; i++) {
            char c = line.charAt(i);
            if (c == '[' || c == '{') {
                depth++;
            } else if (c == ']' || c == '}') {
                depth--;
            } else if (c == '"') {
                int close = closingQuote(line, i);
                int colon = skipWhitespace(line, close + 1);
                if (depth == 0 && colon < line.length() && line.charAt(colon) == ':') {
                    int open = skipWhitespace(line, colon + 1);
                    if (line.substring(i + 1, close).equals(TEXT) && open < line.length() && line.charAt(open) == '"') {
                        return Optional.of(new Span(open + 1, closingQuote(line, open)));
                    }
                }
                i = close;
            }
        }
        return Optional.empty();
    }

    private static int skipWhitespace(String line, int from) {
        int i = from;
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        return i;
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

    private static String decode(String raw) {
        try {
            return JsonParser.parseString("\"" + raw + "\"").getAsString();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Optional<JsonElement> parse(String json) {
        try {
            return Optional.of(JsonParser.parseString(json));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static String write(JsonElement element) {
        return JsonSpacing.spaced(element.toString());
    }

    private static boolean isString(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }
}
