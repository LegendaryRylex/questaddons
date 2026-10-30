package dev.rylex.questaddons.client;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.util.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;

public final class KeybindMenu {
    private static final int MAX_NAME_WIDTH = 200;
    private static final int MAX_CATEGORY_WIDTH = 100;

    private KeybindMenu() {}

    public static List<ContextMenuItem> items(Consumer<String> onPicked) {
        return Arrays.stream(Minecraft.getInstance().options.keyMappings)
                .sorted()
                .map(key -> new ContextMenuItem(label(key), Icon.empty(), button -> onPicked.accept(element(key))))
                .toList();
    }

    private static String element(KeyMapping key) {
        Component defaultKey =
                key.getDefaultKeyModifier().getCombinedName(key.getDefaultKey(), key.getDefaultKey()::getDisplayName);
        return KeybindInsertion.element(key.getName(), defaultKey.getString());
    }

    private static Component label(KeyMapping key) {
        Font font = Minecraft.getInstance().font;
        String name = font.plainSubstrByWidth(I18n.get(key.getName()), MAX_NAME_WIDTH);
        String category = font.plainSubstrByWidth(I18n.get(key.getCategory()), MAX_CATEGORY_WIDTH);
        return Component.literal(name).append(Component.literal("  " + category).withStyle(ChatFormatting.DARK_GRAY));
    }

    public static String plainLineToJson(String marked, String element, HolderLookup.Provider provider) {
        Component parsed = TextUtils.parseRawText(marked, provider);
        if (parsed.getString().indexOf(KeybindInsertion.MARKER) < 0) {
            parsed =
                    Component.empty().append(parsed).append(Component.literal(String.valueOf(KeybindInsertion.MARKER)));
        }

        List<String> parts = new ArrayList<>();
        parsed.visit(
                (style, text) -> {
                    int marker = text.indexOf(KeybindInsertion.MARKER);
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
            parts.add(JsonSpacing.spaced(
                    encode(Component.literal(text).withStyle(style), provider).toString()));
        }
    }

    private static JsonElement encode(Component component, HolderLookup.Provider provider) {
        return ComponentSerialization.CODEC
                .encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), component)
                .getOrThrow();
    }
}
