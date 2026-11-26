package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class EditorButton extends SimpleTextButton {
    private final Consumer<EditorButton> action;
    private final @Nullable KeyMapping hotkey;
    private final List<Component> tooltip;

    public EditorButton(
            Panel panel,
            Icon<?> icon,
            Consumer<EditorButton> action,
            @Nullable KeyMapping hotkey,
            Component... tooltip) {
        super(panel, Component.empty(), icon);
        this.action = action;
        this.hotkey = hotkey;
        this.tooltip = List.of(tooltip);
    }

    public boolean pressedBy(Key key) {
        return hotkey != null && key.matches(hotkey);
    }

    @Override
    public void onClicked(MouseButton button) {
        action.accept(this);
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        tooltip.forEach(list::add);
        if (hotkey != null) {
            QuestAddonsKeys.hint(hotkey).ifPresent(list::add);
        }
    }
}
