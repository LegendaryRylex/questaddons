package dev.rylex.questaddons.client;

public final class JsonSpacing {
    private JsonSpacing() {}

    /** Expects strict JSON; whitespace inside an unquoted token is dropped. */
    public static String spaced(String json) {
        StringBuilder out = new StringBuilder(json.length() + 16);
        int i = 0;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            switch (c) {
                case '"' -> {
                    int end = stringEnd(json, i);
                    out.append(json, i, end);
                    i = end;
                }
                case '{', '[' -> {
                    char closer = c == '{' ? '}' : ']';
                    int next = skipWhitespace(json, i + 1);
                    if (next < json.length() && json.charAt(next) == closer) {
                        out.append(c).append(closer);
                        i = next + 1;
                    } else {
                        out.append(c).append(' ');
                        i++;
                    }
                }
                case '}', ']' -> {
                    out.append(' ').append(c);
                    i++;
                }
                case ',', ':' -> {
                    out.append(c).append(' ');
                    i++;
                }
                default -> {
                    out.append(c);
                    i++;
                }
            }
        }
        return out.toString();
    }

    private static int stringEnd(String json, int open) {
        int i = open + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\') {
                i += 2;
            } else if (c == '"') {
                return i + 1;
            } else {
                i++;
            }
        }
        return json.length();
    }

    private static int skipWhitespace(String json, int from) {
        int i = from;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }
        return i;
    }
}
