package dev.rylex.questaddons.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class SearchSnippet {
    private static final String ELLIPSIS = "…";

    private SearchSnippet() {}

    private record Span(int start, int end) {}

    public static int firstHit(String text, List<String> terms) {
        List<String> highlighted = highlightTerms(terms);
        if (highlighted.isEmpty()) {
            return -1;
        }
        for (Span word : words(text)) {
            if (isHit(text.substring(word.start(), word.end()), highlighted)) {
                return word.start();
            }
        }
        return -1;
    }

    public static Component excerpt(String text, List<String> terms, Font font, int width, Style base, Style hit) {
        List<String> highlighted = highlightTerms(terms);
        int first = firstHit(text, terms);
        int start = 0;
        if (first > 0 && font.width(text.substring(0, first)) > width / 3) {
            start = first;
            List<Span> words = words(text);
            for (int i = words.size() - 1; i >= 0; i--) {
                Span word = words.get(i);
                if (word.start() >= first) {
                    continue;
                }
                if (font.width(text.substring(word.start(), first)) > width / 4) {
                    break;
                }
                start = word.start();
            }
        }
        String lead = start > 0 ? ELLIPSIS : "";
        String body = text.substring(start);
        int room = width - font.width(lead);
        String shown = font.plainSubstrByWidth(body, room);
        boolean cut = shown.length() < body.length();
        if (cut) {
            shown = font.plainSubstrByWidth(body, room - font.width(ELLIPSIS));
        }
        MutableComponent out = Component.literal(lead).withStyle(base);
        int pos = 0;
        for (Span word : words(shown)) {
            String part = shown.substring(word.start(), word.end());
            if (isHit(part, highlighted)) {
                if (word.start() > pos) {
                    out.append(Component.literal(shown.substring(pos, word.start()))
                            .withStyle(base));
                }
                out.append(Component.literal(part).withStyle(hit));
                pos = word.end();
            }
        }
        if (pos < shown.length()) {
            out.append(Component.literal(shown.substring(pos)).withStyle(base));
        }
        if (cut) {
            out.append(Component.literal(ELLIPSIS).withStyle(base));
        }
        return out;
    }

    private static List<String> highlightTerms(List<String> terms) {
        List<String> longer = terms.stream().filter(t -> t.length() > 1).toList();
        return longer.isEmpty() ? terms : longer;
    }

    private static boolean isHit(String word, List<String> terms) {
        String lower = word.toLowerCase(Locale.ROOT);
        for (String term : terms) {
            if (lower.startsWith(term)) {
                return true;
            }
        }
        return false;
    }

    private static List<Span> words(String text) {
        List<Span> words = new ArrayList<>();
        int start = -1;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            boolean letter = Character.isLetterOrDigit(codePoint);
            if (letter && start < 0) {
                start = i;
            } else if (!letter && start >= 0) {
                words.add(new Span(start, i));
                start = -1;
            }
            i += Character.charCount(codePoint);
        }
        if (start >= 0) {
            words.add(new Span(start, text.length()));
        }
        return words;
    }
}
