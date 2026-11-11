package dev.rylex.questaddons.client;

import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftbquests.util.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;

public final class ComponentJson {
    private ComponentJson() {}

    public static String encode(Component component, HolderLookup.Provider provider) {
        return JsonSpacing.spaced(ComponentSerialization.CODEC
                .encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), component)
                .getOrThrow()
                .toString());
    }

    public static String plainLineToJson(String marked, String element, HolderLookup.Provider provider) {
        Component parsed = TextUtils.parseRawText(marked, provider);
        if (parsed.getString().indexOf(ElementInsertion.MARKER) < 0) {
            parsed =
                    Component.empty().append(parsed).append(Component.literal(String.valueOf(ElementInsertion.MARKER)));
        }

        List<String> parts = new ArrayList<>();
        parsed.visit(
                (style, text) -> {
                    int marker = text.indexOf(ElementInsertion.MARKER);
                    if (marker < 0) {
                        addLiteral(parts, text, style, provider);
                    } else {
                        addLiteral(parts, text.substring(0, marker), style, provider);
                        parts.add(element);
                        addLiteral(parts, text.substring(marker + 1), style, provider);
                    }
                    return Optional.empty();
                },
                Style.EMPTY);

        if (parts.getFirst().startsWith("{")) {
            parts.addFirst("\"\"");
        }
        return "[ " + String.join(", ", parts) + " ]";
    }

    private static void addLiteral(List<String> parts, String text, Style style, HolderLookup.Provider provider) {
        if (!text.isEmpty()) {
            parts.add(encode(Component.literal(text).withStyle(style), provider));
        }
    }
}
