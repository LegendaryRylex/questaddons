package dev.rylex.questaddons.compat.ftbfiltersystem;

import java.util.Collection;
import java.util.StringJoiner;

public final class FilterSyntax {
    private FilterSyntax() {}

    public static String or(Collection<String> terms) {
        StringJoiner filter = new StringJoiner("", "or(", ")");
        terms.forEach(filter::add);
        return filter.toString();
    }

    public static String item(String itemId) {
        return "item(" + itemId + ")";
    }
}
