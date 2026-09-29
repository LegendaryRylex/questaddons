package dev.rylex.questaddons.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ComponentJsonRepair {
    private static final String CLICK_EVENT = "click_event";
    private static final String HOVER_EVENT = "hover_event";
    private static final Set<String> CONTENT_KEYS =
            Set.of("text", "translate", "score", "selector", "keybind", "nbt", "object", "sprite", "player");
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
    private static final String QUEST_LINK = "ftbquests:link";
    private static final String OPEN_DOCS = "ftbquests:docs";
    private static final String DOCS_PREFIX = "docs:";
    private static final Map<String, String> KEYS = new HashMap<>();

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
                "shadow_color",
                "object",
                "sprite",
                "atlas",
                "player",
                "hat",
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
            return json.getAsJsonPrimitive().isString() ? json : new JsonPrimitive(json.getAsString());
        }
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.isEmpty()) {
                return new JsonPrimitive("");
            }
            JsonArray out = new JsonArray();
            array.forEach(element -> out.add(component(element)));
            return out;
        }
        return object(json.getAsJsonObject());
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
        if (out.has("extra") && out.getAsJsonArray("extra").isEmpty()) {
            out.remove("extra");
        }
        return out;
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
        in.forEach(element -> out.add(components || !element.isJsonPrimitive() ? component(element) : element));
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
        String action = event.has("action") ? event.get("action").getAsString() : "";
        switch (action) {
            case "open_url" -> {
                renameValue(event, "url");
                if (event.has("url") && event.get("url").getAsString().startsWith(DOCS_PREFIX)) {
                    JsonObject payload = new JsonObject();
                    payload.addProperty(
                            "location", event.remove("url").getAsString().substring(DOCS_PREFIX.length()));
                    return custom(OPEN_DOCS, payload);
                }
            }
            case "open_file" -> renameValue(event, "path");
            case "run_command", "suggest_command" -> renameValue(event, "command");
            case "change_page" -> {
                renameValue(event, "page");
                if (event.has("page") && event.get("page").isJsonPrimitive()) {
                    String page = event.get("page").getAsString().trim();
                    if (page.matches("\\d+")) {
                        event.addProperty("page", Integer.parseInt(page));
                    } else {
                        JsonObject payload = new JsonObject();
                        payload.addProperty("quest_id", page);
                        payload.addProperty("page", 1);
                        return custom(QUEST_LINK, payload);
                    }
                }
            }
            default -> {}
        }
        return event;
    }

    private static JsonObject custom(String id, JsonObject payload) {
        JsonObject event = new JsonObject();
        event.addProperty("action", "custom");
        event.addProperty("id", id);
        event.add("payload", payload);
        return event;
    }

    private static void renameValue(JsonObject event, String key) {
        if (!event.has(key) && event.has("value")) {
            event.add(key, primitiveAsString(event.remove("value")));
        }
    }

    private static JsonElement hoverEvent(JsonElement value) {
        if (!value.isJsonObject()) {
            return value;
        }
        JsonObject event = lowercaseAction(value.getAsJsonObject());
        String action = event.has("action") ? event.get("action").getAsString() : "";
        switch (action) {
            case "show_text" -> {
                JsonElement text = event.has("value") ? event.get("value") : event.remove("contents");
                event.add("value", component(text));
            }
            case "show_item" -> {
                if (event.has("contents")) {
                    JsonElement contents = event.remove("contents");
                    if (contents.isJsonObject()) {
                        moveInto(contents.getAsJsonObject(), List.of("id", "count", "components"), Map.of())
                                .entrySet()
                                .forEach(entry -> event.add(entry.getKey(), entry.getValue()));
                    } else {
                        event.add("id", contents);
                    }
                }
            }
            case "show_entity" -> {
                if (event.has("contents") && event.get("contents").isJsonObject()) {
                    JsonObject contents = event.remove("contents").getAsJsonObject();
                    moveInto(contents, List.of("type", "id", "name"), Map.of("type", "id", "id", "uuid"))
                            .entrySet()
                            .forEach(entry -> event.add(entry.getKey(), entry.getValue()));
                }
                if (event.has("name")) {
                    event.add("name", component(event.get("name")));
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
