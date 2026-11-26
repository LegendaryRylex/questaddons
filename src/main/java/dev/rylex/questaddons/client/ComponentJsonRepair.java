package dev.rylex.questaddons.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ComponentJsonRepair {
    private static final String CLICK_EVENT = "clickEvent";
    private static final String HOVER_EVENT = "hoverEvent";
    private static final Set<String> CONTENT_KEYS = Set.of("text", "translate", "score", "selector", "keybind", "nbt");
    private static final Set<String> FLAGS = Set.of("bold", "italic", "underlined", "strikethrough", "obfuscated");
    private static final Set<String> STRING_KEYS =
            Set.of("text", "translate", "fallback", "keybind", "insertion", "font");
    private static final Set<String> NAMED_COLORS = Set.of(
            "black",
            "dark_blue",
            "dark_green",
            "dark_aqua",
            "dark_red",
            "dark_purple",
            "gold",
            "gray",
            "dark_gray",
            "blue",
            "green",
            "aqua",
            "red",
            "light_purple",
            "yellow",
            "white");
    private static final List<String> CLICK_VALUE_KEYS = List.of("url", "command", "page", "path");
    private static final Map<String, String> KEYS = new HashMap<>();
    private static final Pattern STRAY_WRAPPER =
            Pattern.compile("\\{\\s*\"text\"\\s*:\\s*\"(.*?)(?:\"\\s*\\})?", Pattern.DOTALL);

    static {
        for (String key : List.of(
                "text",
                "translate",
                "with",
                "fallback",
                "score",
                "selector",
                "separator",
                "keybind",
                "nbt",
                "interpret",
                "block",
                "entity",
                "storage",
                "source",
                "type",
                "color",
                "font",
                "insertion",
                "extra")) {
            KEYS.put(key, key);
        }
        FLAGS.forEach(flag -> KEYS.put(flag, flag));
        KEYS.put("underline", "underlined");
        KEYS.put("colour", "color");
        KEYS.put("clickevent", CLICK_EVENT);
        KEYS.put("click_event", CLICK_EVENT);
        KEYS.put("hoverevent", HOVER_EVENT);
        KEYS.put("hover_event", HOVER_EVENT);
    }

    private ComponentJsonRepair() {}

    public static boolean looksLikeComponent(String line) {
        if (line.length() < 2) {
            return false;
        }
        int next = skipWhitespace(line, 1);
        if (next == line.length()) {
            return false;
        }
        char c = line.charAt(next);
        if (line.charAt(0) == '[') {
            return c == '{' || c == '[' || LenientJson.isQuote(c);
        }
        if (line.charAt(0) != '{') {
            return false;
        }
        if (LenientJson.isQuote(c)) {
            return true;
        }
        int end = next;
        while (end < line.length() && (Character.isLetterOrDigit(line.charAt(end)) || line.charAt(end) == '_')) {
            end++;
        }
        int after = skipWhitespace(line, end);
        return KEYS.containsKey(line.substring(next, end).toLowerCase(Locale.ROOT))
                && after < line.length()
                && (line.charAt(after) == ':' || line.charAt(after) == '=');
    }

    public static JsonElement repairLine(JsonElement json) {
        JsonElement repaired = component(json);
        if (repaired.isJsonPrimitive()) {
            JsonObject wrapped = new JsonObject();
            wrapped.add("text", repaired);
            return wrapped;
        }
        return repaired;
    }

    static JsonElement component(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return new JsonPrimitive("");
        }
        if (json.isJsonPrimitive()) {
            return isString(json) ? legacy(json.getAsString()) : new JsonPrimitive(json.getAsString());
        }
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.isEmpty()) {
                return new JsonPrimitive("");
            }
            List<JsonElement> elements = new ArrayList<>();
            array.forEach(element -> {
                Optional<JsonObject> unwrapped = strayWrapper(element);
                if (unwrapped.isPresent() && !elements.isEmpty() && plainCopyOf(elements.getLast(), unwrapped.get())) {
                    elements.removeLast();
                }
                elements.add(unwrapped.map(JsonElement.class::cast).orElse(element));
            });
            JsonArray out = new JsonArray();
            elements.forEach(element -> splice(out, element, true));
            return out.isEmpty() ? new JsonPrimitive("") : out;
        }
        return object(strayWrapper(json).orElse(json.getAsJsonObject()));
    }

    private static Optional<JsonObject> strayWrapper(JsonElement element) {
        if (!element.isJsonObject()
                || !element.getAsJsonObject().has("text")
                || !isString(element.getAsJsonObject().get("text"))) {
            return Optional.empty();
        }
        Matcher matcher =
                STRAY_WRAPPER.matcher(element.getAsJsonObject().get("text").getAsString());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        JsonObject unwrapped = element.getAsJsonObject().deepCopy();
        unwrapped.addProperty("text", unescaped(matcher.group(1)));
        return Optional.of(unwrapped);
    }

    private static String unescaped(String raw) {
        try {
            return JsonParser.parseString("\"" + raw + "\"").getAsString();
        } catch (RuntimeException e) {
            return raw;
        }
    }

    private static boolean plainCopyOf(JsonElement previous, JsonObject styled) {
        JsonObject plain;
        if (isString(previous)) {
            plain = new JsonObject();
            plain.add("text", previous);
        } else if (previous.isJsonObject()) {
            plain = previous.getAsJsonObject();
        } else {
            return false;
        }
        return plain.has("text")
                && plain.entrySet().stream().allMatch(entry -> entry.getValue().equals(styled.get(entry.getKey())));
    }

    private static JsonElement legacy(String text) {
        Optional<List<JsonElement>> parts = LegacyFormatting.split(text);
        if (parts.isEmpty()) {
            return new JsonPrimitive(text);
        }
        if (parts.get().size() == 1) {
            return parts.get().getFirst();
        }
        JsonArray out = new JsonArray();
        splice(out, new JsonPrimitive(text), true);
        return out.isEmpty() ? new JsonPrimitive("") : out;
    }

    private static void splice(JsonArray out, JsonElement element, boolean parentFirst) {
        Optional<List<JsonElement>> parts =
                isString(element) ? LegacyFormatting.split(element.getAsString()) : Optional.empty();
        if (parts.isEmpty()) {
            out.add(component(element));
            return;
        }
        List<JsonElement> list = parts.get();
        if (parentFirst && out.isEmpty() && !list.isEmpty() && !list.getFirst().isJsonPrimitive()) {
            out.add("");
        }
        list.forEach(out::add);
    }

    private static void moveIntoExtra(JsonObject out, List<JsonElement> parts) {
        JsonArray extra = new JsonArray();
        int from = 0;
        if (!parts.isEmpty() && parts.getFirst().isJsonPrimitive()) {
            out.add("text", parts.getFirst());
            from = 1;
        } else {
            out.addProperty("text", "");
        }
        parts.subList(from, parts.size()).forEach(extra::add);
        if (out.has("extra")) {
            out.getAsJsonArray("extra").forEach(extra::add);
        }
        out.add("extra", extra);
    }

    private static boolean isString(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    private static JsonObject object(JsonObject in) {
        JsonObject renamed = new JsonObject();
        in.entrySet().stream()
                .filter(entry -> canonicalKey(entry.getKey()).equals(entry.getKey()))
                .forEach(entry -> renamed.add(entry.getKey(), entry.getValue()));
        in.entrySet().stream()
                .filter(entry -> !canonicalKey(entry.getKey()).equals(entry.getKey()))
                .filter(entry -> !renamed.has(canonicalKey(entry.getKey())))
                .forEach(entry -> renamed.add(canonicalKey(entry.getKey()), entry.getValue()));

        JsonObject out = new JsonObject();
        if (CONTENT_KEYS.stream().noneMatch(renamed::has)) {
            out.addProperty("text", "");
        }
        renamed.entrySet().forEach(entry -> out.add(entry.getKey(), value(entry.getKey(), entry.getValue())));
        if (out.has("text") && isString(out.get("text"))) {
            Optional<List<JsonElement>> parts =
                    LegacyFormatting.split(out.get("text").getAsString());
            if (parts.isPresent() && canHoist(out, parts.get())) {
                return hoist(out, parts.get().getFirst().getAsJsonObject());
            }
            parts.ifPresent(list -> moveIntoExtra(out, list));
        }
        if (out.has("extra") && out.getAsJsonArray("extra").isEmpty()) {
            out.remove("extra");
        }
        return out;
    }

    private static boolean canHoist(JsonObject out, List<JsonElement> parts) {
        return parts.size() == 1 && parts.getFirst().isJsonObject() && !out.has("extra");
    }

    private static JsonObject hoist(JsonObject out, JsonObject part) {
        JsonObject flat = new JsonObject();
        part.entrySet().forEach(entry -> flat.add(entry.getKey(), entry.getValue()));
        out.entrySet().stream()
                .filter(entry -> !flat.has(entry.getKey()))
                .forEach(entry -> flat.add(entry.getKey(), entry.getValue()));
        return flat;
    }

    private static JsonElement value(String key, JsonElement value) {
        if (FLAGS.contains(key)) {
            return flag(value);
        }
        if (STRING_KEYS.contains(key)) {
            return value.isJsonNull() ? new JsonPrimitive("") : primitiveAsString(value);
        }
        return switch (key) {
            case "color" -> color(value);
            case "extra" -> list(value, true);
            case "with" -> list(value, false);
            case "separator" -> component(value);
            case CLICK_EVENT -> clickEvent(value);
            case HOVER_EVENT -> hoverEvent(value);
            default -> value;
        };
    }

    private static JsonArray list(JsonElement value, boolean components) {
        JsonArray in = value.isJsonArray() ? value.getAsJsonArray() : singleton(value);
        JsonArray out = new JsonArray();
        in.forEach(element -> {
            if (components) {
                splice(out, element, false);
            } else {
                out.add(element.isJsonPrimitive() ? element : component(element));
            }
        });
        return out;
    }

    private static JsonElement flag(JsonElement value) {
        if (!value.isJsonPrimitive()) {
            return value;
        }
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive;
        }
        if (primitive.isNumber()) {
            return new JsonPrimitive(primitive.getAsDouble() != 0);
        }
        return switch (primitive.getAsString().trim().toLowerCase(Locale.ROOT)) {
            case "true", "yes", "1" -> new JsonPrimitive(true);
            case "false", "no", "0" -> new JsonPrimitive(false);
            default -> primitive;
        };
    }

    static JsonElement color(JsonElement value) {
        if (!value.isJsonPrimitive()) {
            return value;
        }
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return new JsonPrimitive("#%06X".formatted(primitive.getAsInt() & 0xFFFFFF));
        }
        String raw = primitive.getAsString();
        if (NAMED_COLORS.contains(raw) || raw.matches("#[0-9a-fA-F]{6}")) {
            return primitive;
        }
        String name = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_')
                .replace("grey", "gray");
        if (NAMED_COLORS.contains(name)) {
            return new JsonPrimitive(name);
        }
        String hex = name.startsWith("#") ? name.substring(1) : name.startsWith("0x") ? name.substring(2) : name;
        if (hex.matches("[0-9a-f]{6}")) {
            return new JsonPrimitive("#" + hex.toUpperCase(Locale.ROOT));
        }
        if (hex.matches("[0-9a-f]{3}")) {
            StringBuilder doubled = new StringBuilder("#");
            hex.chars().forEach(c -> doubled.append((char) c).append((char) c));
            return new JsonPrimitive(doubled.toString().toUpperCase(Locale.ROOT));
        }
        return primitive;
    }

    private static JsonElement clickEvent(JsonElement value) {
        if (!value.isJsonObject()) {
            return value;
        }
        JsonObject event = lowercaseAction(value.getAsJsonObject());
        if (!event.has("value")) {
            CLICK_VALUE_KEYS.stream()
                    .filter(event::has)
                    .findFirst()
                    .ifPresent(key -> event.add("value", event.remove(key)));
        }
        if (event.has("value")) {
            event.add("value", primitiveAsString(event.get("value")));
        }
        return event;
    }

    private static JsonElement hoverEvent(JsonElement value) {
        if (!value.isJsonObject()) {
            return value;
        }
        JsonObject event = lowercaseAction(value.getAsJsonObject());
        String action = event.has("action") ? event.get("action").getAsString() : "";
        switch (action) {
            case "show_text" -> {
                JsonElement text = event.has("contents") ? event.get("contents") : event.remove("value");
                event.add("contents", component(text));
            }
            case "show_item" -> {
                if (!event.has("contents") && event.has("id")) {
                    event.add("contents", moveInto(event, List.of("id", "count", "components"), Map.of()));
                }
            }
            case "show_entity" -> {
                if (!event.has("contents") && event.has("id")) {
                    JsonObject contents =
                            moveInto(event, List.of("id", "uuid", "name"), Map.of("id", "type", "uuid", "id"));
                    if (contents.has("name")) {
                        contents.add("name", component(contents.get("name")));
                    }
                    event.add("contents", contents);
                }
            }
            default -> {}
        }
        return event;
    }

    private static JsonObject moveInto(JsonObject from, List<String> keys, Map<String, String> renames) {
        JsonObject into = new JsonObject();
        keys.stream().filter(from::has).forEach(key -> into.add(renames.getOrDefault(key, key), from.remove(key)));
        return into;
    }

    private static JsonObject lowercaseAction(JsonObject event) {
        JsonObject copy = event.deepCopy();
        if (copy.has("action") && copy.get("action").isJsonPrimitive()) {
            copy.addProperty("action", copy.get("action").getAsString().trim().toLowerCase(Locale.ROOT));
        }
        return copy;
    }

    private static JsonElement primitiveAsString(JsonElement value) {
        return value.isJsonPrimitive() && !value.getAsJsonPrimitive().isString()
                ? new JsonPrimitive(value.getAsString())
                : value;
    }

    private static JsonArray singleton(JsonElement value) {
        JsonArray array = new JsonArray();
        array.add(value);
        return array;
    }

    private static String canonicalKey(String key) {
        return KEYS.getOrDefault(key.toLowerCase(Locale.ROOT), key);
    }

    private static int skipWhitespace(String line, int from) {
        int i = from;
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        return i;
    }
}
