package dev.rylex.questaddons.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ComponentSerialization;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

public final class DescriptionJsonFixer {
    private static final UnicodeUnescaper UNESCAPER = new UnicodeUnescaper();

    private DescriptionJsonFixer() {}

    /** Line numbers in {@code unfixable} are 1-based. */
    public record Result(String text, int fixed, List<Integer> unfixable) {}

    public static Result fix(String text, HolderLookup.Provider provider) {
        String[] lines = text.split("\n", -1);
        int fixed = 0;
        List<Integer> unfixable = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!ComponentJsonRepair.looksLikeComponent(line)) {
                continue;
            }
            Optional<JsonElement> repaired =
                    LenientJson.parse(line).map(ComponentJsonRepair::repairLine).filter(json -> parses(json, provider));
            if (repaired.isEmpty()) {
                unfixable.add(i + 1);
            } else if (!alreadyWorks(line, repaired.get(), provider)) {
                lines[i] = repaired.get().toString();
                fixed++;
            }
        }
        return new Result(String.join("\n", lines), fixed, List.copyOf(unfixable));
    }

    private static boolean alreadyWorks(String line, JsonElement repaired, HolderLookup.Provider provider) {
        boolean bracketed =
                (line.startsWith("[") && line.endsWith("]")) || (line.startsWith("{") && line.endsWith("}"));
        if (!bracketed) {
            return false;
        }
        try {
            JsonElement current = JsonParser.parseString(UNESCAPER.translate(line));
            return current.equals(repaired) && parses(current, provider);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean parses(JsonElement json, HolderLookup.Provider provider) {
        try {
            return ComponentSerialization.CODEC
                    .parse(provider.createSerializationContext(JsonOps.INSTANCE), json)
                    .isSuccess();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
