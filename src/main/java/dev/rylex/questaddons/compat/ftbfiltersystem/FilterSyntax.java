package dev.rylex.questaddons.compat.ftbfiltersystem;

import java.util.Collection;
import java.util.StringJoiner;

public final class FilterSyntax {
    private FilterSyntax() {}

    public static String or(Collection<String> itemIds) {
        StringJoiner terms = new StringJoiner("", "or(", ")");
        itemIds.forEach(id -> terms.add(item(id)));
        return terms.toString();
    }

    public static String item(String itemId) {
        return "item(" + itemId + ")";
    }
}
