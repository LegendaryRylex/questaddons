package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.icon.Icon;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class KeybindMenu {
    private KeybindMenu() {}

    public static List<ContextMenuItem> items(Consumer<String> onPicked) {
        return Arrays.stream(Minecraft.getInstance().options.keyMappings)
                .sorted()
                .map(key -> new ContextMenuItem(
                        MenuLabel.of(
                                I18n.get(key.getName()),
                                key.getCategory().label().getString()),
                        Icon.empty(),
                        button -> onPicked.accept(element(key))))
                .toList();
    }

    private static String element(KeyMapping key) {
        Component defaultKey =
                key.getDefaultKeyModifier().getCombinedName(key.getDefaultKey(), key.getDefaultKey()::getDisplayName);
        return ElementInsertion.keybind(key.getName(), defaultKey.getString());
    }
}
