package dev.rylex.questaddons.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ComponentJsonRepair {
    private static final String CLICK_EVENT = "clickEvent";
    private static final String TOOLTIP_TEXT = "contents";
    private static final List<String> IDENTITY_KEYS = List.of("with", "fallback", "separator");
    private static final String INSERTION = "insertion";
    private static final String ENTITY_TYPE = "type";
    private static final Map<String, String> CLICK_TARGETS = Map.of(
            "open_url", "value",
            "open_file", "value",
            "run_command", "value",
            "suggest_command", "value",
            "change_page", "value",
            "copy_to_clipboard", "value");
    private static final String HOVER_EVENT = "hoverEvent";
    private static final List<String> INTERACTION_KEYS = List.of(CLICK_EVENT, HOVER_EVENT, INSERTION);
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
        return repairLine(json, key -> Optional.empty());
    }

    public static JsonElement repairLine(JsonElement json, Function<String, Optional<String>> names) {
        JsonElement repaired = component(json);
        if (repaired.isJsonPrimitive()) {
            JsonObject wrapped = new JsonObject();
            wrapped.add("text", repaired);
            return wrapped;
        }
        walk(repaired, names);
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
        dropDeadEvents(out);
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

    private static void walk(JsonElement json, Function<String, Optional<String>> names) {
        if (json.isJsonArray()) {
            JsonArray parts = json.getAsJsonArray();
            parts.forEach(part -> walk(part, names));
            mergeSiblings(parts, true, names);
            return;
        }
        if (!json.isJsonObject()) {
            return;
        }
        JsonObject object = json.getAsJsonObject();
        if (object.has("with") && object.get("with").isJsonArray()) {
            object.getAsJsonArray("with").forEach(argument -> walk(argument, names));
        }
        if (object.has(HOVER_EVENT)
                && object.get(HOVER_EVENT).isJsonObject()
                && action(object.getAsJsonObject(HOVER_EVENT)).equals("show_text")
                && object.getAsJsonObject(HOVER_EVENT).has(TOOLTIP_TEXT)) {
            walk(object.getAsJsonObject(HOVER_EVENT).get(TOOLTIP_TEXT), names);
        }
        if (object.has("extra") && object.get("extra").isJsonArray()) {
            JsonArray extra = object.getAsJsonArray("extra");
            extra.forEach(part -> walk(part, names));
            mergeSiblings(extra, false, names);
        }
    }

    private static void mergeSiblings(JsonArray parts, boolean parentFirst, Function<String, Optional<String>> names) {
        for (int i = parts.size() - 2; i >= (parentFirst ? 1 : 0); i--) {
            Optional<JsonObject> moved = movedOntoNext(parts.get(i), parts.get(i + 1));
            if (moved.isPresent()) {
                parts.set(i + 1, moved.get());
                parts.remove(i);
            }
        }
        for (int i = parts.size() - 1; i > 0; i--) {
            JsonElement first = parts.get(i - 1);
            Optional<JsonObject> merged = merged(first, parts.get(i), names);
            if (merged.isEmpty()) {
                continue;
            }
            parts.set(i - 1, merged.get());
            parts.remove(i);
            if (parentFirst
                    && i == 1
                    && parts.size() > 1
                    && !merged.get().equals(objectOf(first).orElse(null))) {
                parts.asList().add(0, parentStyle(first));
            }
        }
    }

    private static JsonElement parentStyle(JsonElement first) {
        if (!first.isJsonObject()) {
            return new JsonPrimitive("");
        }
        JsonObject style = new JsonObject();
        style.addProperty("text", "");
        first.getAsJsonObject().entrySet().stream()
                .filter(entry -> !CONTENT_KEYS.contains(entry.getKey()) && !IDENTITY_KEYS.contains(entry.getKey()))
                .forEach(entry -> style.add(entry.getKey(), entry.getValue()));
        return style;
    }

    private static Optional<JsonObject> movedOntoNext(JsonElement empty, JsonElement next) {
        Optional<JsonObject> source = objectOf(empty);
        Optional<JsonObject> target = objectOf(next);
        if (source.isEmpty()
                || target.isEmpty()
                || !blank(source.get())
                || !interactive(source.get())
                || blank(target.get())) {
            return Optional.empty();
        }
        JsonObject out = target.get().deepCopy();
        for (String key : INTERACTION_KEYS) {
            JsonElement value = source.get().get(key);
            if (value == null) {
                continue;
            }
            if (out.has(key) && !out.get(key).equals(value)) {
                return Optional.empty();
            }
            out.add(key, value);
        }
        return Optional.of(out);
    }

    private static Optional<JsonObject> merged(
            JsonElement first, JsonElement second, Function<String, Optional<String>> names) {
        Optional<JsonObject> a = objectOf(first);
        Optional<JsonObject> b = objectOf(second);
        if (a.isEmpty()
                || b.isEmpty()
                || a.get().has("extra")
                || b.get().has("extra")
                || blank(a.get())
                || blank(b.get())
                || !(interactive(a.get()) || interactive(b.get()))) {
            return Optional.empty();
        }
        Optional<JsonObject> content = sharedContent(a.get(), b.get(), names);
        if (content.isEmpty()) {
            return Optional.empty();
        }
        JsonObject out = content.get().deepCopy();
        Set<String> keys = new LinkedHashSet<>(a.get().keySet());
        keys.addAll(b.get().keySet());
        for (String key : keys) {
            if (CONTENT_KEYS.contains(key) || IDENTITY_KEYS.contains(key)) {
                continue;
            }
            JsonElement left = a.get().get(key);
            JsonElement right = b.get().get(key);
            Optional<JsonElement> value = left == null
                    ? Optional.of(right)
                    : right == null || left.equals(right)
                            ? Optional.of(left)
                            : key.equals(HOVER_EVENT) ? richerHover(left, right) : Optional.empty();
            if (value.isEmpty()) {
                return Optional.empty();
            }
            out.add(key, value.get());
        }
        return Optional.of(out);
    }

    private static Optional<JsonObject> sharedContent(
            JsonObject a, JsonObject b, Function<String, Optional<String>> names) {
        JsonObject left = content(a);
        JsonObject right = content(b);
        if (!left.isEmpty() && left.equals(right)) {
            return Optional.of(left);
        }
        if (namedAs(left, right, names)) {
            return Optional.of(left);
        }
        if (namedAs(right, left, names)) {
            return Optional.of(right);
        }
        return Optional.empty();
    }

    private static boolean namedAs(
            JsonObject translated, JsonObject literal, Function<String, Optional<String>> names) {
        return translated.keySet().equals(Set.of("translate"))
                && literal.keySet().equals(Set.of("text"))
                && isString(translated.get("translate"))
                && isString(literal.get("text"))
                && names.apply(translated.get("translate").getAsString())
                        .filter(literal.get("text").getAsString()::equals)
                        .isPresent();
    }

    private static JsonObject content(JsonObject component) {
        JsonObject content = new JsonObject();
        component.entrySet().stream()
                .filter(entry -> CONTENT_KEYS.contains(entry.getKey()) || IDENTITY_KEYS.contains(entry.getKey()))
                .forEach(entry -> content.add(entry.getKey(), entry.getValue()));
        return content;
    }

    private static Optional<JsonElement> richerHover(JsonElement left, JsonElement right) {
        int leftRank = hoverRank(left);
        int rightRank = hoverRank(right);
        if (leftRank == rightRank) {
            return Optional.empty();
        }
        return Optional.of(leftRank > rightRank ? left : right);
    }

    private static int hoverRank(JsonElement event) {
        if (!event.isJsonObject()) {
            return 0;
        }
        return switch (action(event.getAsJsonObject())) {
            case "show_item", "show_entity" -> 2;
            case "show_text" -> 1;
            default -> 0;
        };
    }

    private static boolean interactive(JsonObject component) {
        return INTERACTION_KEYS.stream().anyMatch(component::has);
    }

    private static Optional<JsonObject> objectOf(JsonElement element) {
        if (element.isJsonObject()) {
            return Optional.of(element.getAsJsonObject());
        }
        if (isString(element)) {
            JsonObject object = new JsonObject();
            object.add("text", element);
            return Optional.of(object);
        }
        return Optional.empty();
    }

    private static void dropDeadEvents(JsonObject out) {
        if (out.has(HOVER_EVENT) && deadHover(out.get(HOVER_EVENT))) {
            out.remove(HOVER_EVENT);
        }
        if (out.has(CLICK_EVENT) && deadClick(out.get(CLICK_EVENT))) {
            out.remove(CLICK_EVENT);
        }
        if (out.has(INSERTION) && blank(out.get(INSERTION))) {
            out.remove(INSERTION);
        }
    }

    private static boolean deadHover(JsonElement event) {
        if (!event.isJsonObject()) {
            return false;
        }
        JsonObject object = event.getAsJsonObject();
        return switch (action(object)) {
            case "show_text" -> blank(object.get(TOOLTIP_TEXT));
            case "show_item" -> missingId(hoverTarget(object, "id"));
            case "show_entity" -> missingId(hoverTarget(object, ENTITY_TYPE));
            default -> false;
        };
    }

    private static boolean missingId(Optional<JsonElement> id) {
        return id.map(value -> isString(value) ? value.getAsString().trim() : "?")
                .map(value -> value.isEmpty() || value.equals("air") || value.equals("minecraft:air"))
                .orElse(true);
    }

    private static boolean deadClick(JsonElement event) {
        if (!event.isJsonObject()) {
            return false;
        }
        JsonObject object = event.getAsJsonObject();
        String target = CLICK_TARGETS.get(action(object));
        if (target == null) {
            return false;
        }
        JsonElement value = object.get(target);
        return value == null
                || value.isJsonNull()
                || (isString(value) && value.getAsString().isBlank());
    }

    private static String action(JsonObject event) {
        return isString(event.get("action")) ? event.get("action").getAsString() : "";
    }

    private static boolean blank(JsonElement text) {
        if (text == null || text.isJsonNull()) {
            return true;
        }
        if (isString(text)) {
            return text.getAsString().isEmpty();
        }
        if (text.isJsonArray()) {
            return text.getAsJsonArray().asList().stream().allMatch(ComponentJsonRepair::blank);
        }
        if (!text.isJsonObject()) {
            return false;
        }
        JsonObject object = text.getAsJsonObject();
        return CONTENT_KEYS.stream().filter(object::has).allMatch(key -> key.equals("text"))
                && blank(object.get("text"))
                && blank(object.get("extra"));
    }

    private static Optional<JsonElement> hoverTarget(JsonObject event, String key) {
        if (event.has("value")) {
            return Optional.of(event.get("value"));
        }
        JsonElement contents = event.get("contents");
        if (contents != null && contents.isJsonObject()) {
            return Optional.ofNullable(contents.getAsJsonObject().get(key));
        }
        return Optional.ofNullable(contents);
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
