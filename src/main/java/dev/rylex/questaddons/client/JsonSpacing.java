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
}
