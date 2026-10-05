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
            } else {
                String spaced = spaced(line, repaired.get(), provider);
                if (!spaced.equals(line)) {
                    lines[i] = spaced;
                    fixed++;
                }
            }
        }
        return new Result(String.join("\n", lines), fixed, List.copyOf(unfixable));
    }

    private static String spaced(String line, JsonElement repaired, HolderLookup.Provider provider) {
        if (alreadyWorks(line, repaired, provider)) {
            String respaced = JsonSpacing.spaced(line);
            if (strict(respaced).filter(repaired::equals).isPresent()) {
                return respaced;
            }
        }
        return JsonSpacing.spaced(repaired.toString());
    }

    private static Optional<JsonElement> strict(String line) {
        try {
            return Optional.of(JsonParser.parseString(UNESCAPER.translate(line)));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static boolean alreadyWorks(String line, JsonElement repaired, HolderLookup.Provider provider) {
        boolean bracketed =
                (line.startsWith("[") && line.endsWith("]")) || (line.startsWith("{") && line.endsWith("}"));
        return bracketed
                && strict(line)
                        .filter(current -> current.equals(repaired) && parses(current, provider))
                        .isPresent();
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
