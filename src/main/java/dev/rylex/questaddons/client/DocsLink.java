package dev.rylex.questaddons.client;

import java.util.Comparator;

public record DocsLink(String value, String title, String source) {
    public static final Comparator<DocsLink> ORDER = Comparator.comparing(
                    DocsLink::source, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(DocsLink::title, String.CASE_INSENSITIVE_ORDER);
}
