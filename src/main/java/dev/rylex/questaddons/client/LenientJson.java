package dev.rylex.questaddons.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LenientJson {
    private static final String DOUBLE_QUOTES = "\"“”„";
    private static final String SINGLE_QUOTES = "'‘’";
    private static final String CLOSERS = "}]";
    private static final String VALUE_END = ",;}]:{[";
    private static final Pattern NUMBER = Pattern.compile("[-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?");

    private final String src;
    private int pos;

    private LenientJson(String src) {
        this.src = src;
    }

    public static Optional<JsonElement> parse(String text) {
        try {
            return Optional.of(new LenientJson(text).document());
        } catch (Malformed e) {
            return Optional.empty();
        }
    }

    static boolean isQuote(char c) {
        return DOUBLE_QUOTES.indexOf(c) >= 0 || SINGLE_QUOTES.indexOf(c) >= 0;
    }

    private JsonElement document() {
        List<JsonElement> values = new ArrayList<>();
        while (true) {
            skipWhitespace();
            if (atEnd()) {
                break;
            }
            char c = peek();
            if (c == ',' || c == ';' || CLOSERS.indexOf(c) >= 0) {
                pos++;
                continue;
            }
            values.add(value());
        }
        if (values.isEmpty()) {
            throw new Malformed();
        }
        if (values.size() == 1) {
            return values.getFirst();
        }
        JsonArray joined = new JsonArray();
        joined.add("");
        values.forEach(joined::add);
        return joined;
    }

    private JsonElement value() {
        skipWhitespace();
        if (atEnd()) {
            throw new Malformed();
        }
        char c = peek();
        if (c == '{') {
            return object();
        }
        if (c == '[') {
            return array();
        }
        if (isQuote(c)) {
            return new JsonPrimitive(string());
        }
        JsonElement number = number();
        return number != null ? number : bareValue();
    }

    private JsonObject object() {
        pos++;
        JsonObject object = new JsonObject();
        while (true) {
            skipWhitespace();
            if (atEnd()) {
                return object;
            }
            char c = peek();
            if (CLOSERS.indexOf(c) >= 0) {
                pos++;
                return object;
            }
            if (c == ',' || c == ';') {
                pos++;
                continue;
            }
            String key = key();
            skipWhitespace();
            if (atEnd() || (peek() != ':' && peek() != '=')) {
                throw new Malformed();
            }
            pos++;
            object.add(key, value());
        }
    }

    private JsonArray array() {
        pos++;
        JsonArray array = new JsonArray();
        while (true) {
            skipWhitespace();
            if (atEnd()) {
                return array;
            }
            char c = peek();
            if (CLOSERS.indexOf(c) >= 0) {
                pos++;
                return array;
            }
            if (c == ',' || c == ';') {
                pos++;
                continue;
            }
            array.add(value());
        }
    }

    private String key() {
        if (isQuote(peek())) {
            return string();
        }
        int start = pos;
        while (!atEnd() && isKeyChar(peek())) {
            pos++;
        }
        if (start == pos) {
            throw new Malformed();
        }
        return src.substring(start, pos);
    }

    private String string() {
        char open = src.charAt(pos++);
        String closers = DOUBLE_QUOTES.indexOf(open) >= 0 ? DOUBLE_QUOTES : SINGLE_QUOTES;
        int start = pos;
        StringBuilder out = new StringBuilder();
        while (!atEnd()) {
            char c = src.charAt(pos++);
            if (c == '\\') {
                escape(out);
            } else if (closers.indexOf(c) >= 0 && closesString()) {
                return out.toString();
            } else {
                out.append(c);
            }
        }
        int end = src.length();
        while (end > start
                && (CLOSERS.indexOf(src.charAt(end - 1)) >= 0 || Character.isWhitespace(src.charAt(end - 1)))) {
            end--;
        }
        int dropped = src.length() - end;
        pos = end;
        return out.substring(0, Math.max(0, out.length() - dropped));
    }

    private void escape(StringBuilder out) {
        if (atEnd()) {
            out.append('\\');
            return;
        }
        char e = src.charAt(pos++);
        switch (e) {
            case 'b' -> out.append('\b');
            case 'f' -> out.append('\f');
            case 'n' -> out.append('\n');
            case 'r' -> out.append('\r');
            case 't' -> out.append('\t');
            case 'u' -> {
                if (pos + 4 <= src.length() && src.substring(pos, pos + 4).matches("[0-9a-fA-F]{4}")) {
                    out.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                    pos += 4;
                } else {
                    out.append('u');
                }
            }
            default -> out.append(e);
        }
    }

    private boolean closesString() {
        int next = pos;
        while (next < src.length() && Character.isWhitespace(src.charAt(next))) {
            next++;
        }
        if (next == src.length()) {
            return true;
        }
        char c = src.charAt(next);
        return VALUE_END.indexOf(c) >= 0 || (next > pos && isQuote(c));
    }

    private JsonElement number() {
        Matcher matcher = NUMBER.matcher(src).region(pos, src.length());
        if (!matcher.lookingAt()) {
            return null;
        }
        int end = matcher.end();
        if (end < src.length() && !Character.isWhitespace(src.charAt(end)) && VALUE_END.indexOf(src.charAt(end)) < 0) {
            return null;
        }
        pos = end;
        String digits = matcher.group();
        return new JsonPrimitive(new BigDecimal(digits.startsWith("+") ? digits.substring(1) : digits));
    }

    private JsonElement bareValue() {
        int start = pos;
        while (!atEnd() && ",;}]".indexOf(peek()) < 0) {
            pos++;
        }
        String word = src.substring(start, pos).trim();
        if (word.isEmpty()) {
            throw new Malformed();
        }
        return switch (word.toLowerCase(Locale.ROOT)) {
            case "true" -> new JsonPrimitive(true);
            case "false" -> new JsonPrimitive(false);
            case "null" -> JsonNull.INSTANCE;
            default -> new JsonPrimitive(word);
        };
    }

    private static boolean isKeyChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' || c == '$';
    }

    private void skipWhitespace() {
        while (!atEnd() && Character.isWhitespace(peek())) {
            pos++;
        }
    }

    private boolean atEnd() {
        return pos >= src.length();
    }

    private char peek() {
        return src.charAt(pos);
    }

    private static final class Malformed extends RuntimeException {
        private Malformed() {
            super(null, null, false, false);
        }
    }
}
