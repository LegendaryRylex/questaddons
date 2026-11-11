package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public final class EditorButton extends SimpleTextButton {
    private final Consumer<EditorButton> action;
    private final List<Component> tooltip;

    public EditorButton(Panel panel, Icon<?> icon, Consumer<EditorButton> action, Component... tooltip) {
        super(panel, Component.empty(), icon);
        this.action = action;
        this.tooltip = List.of(tooltip);
    }

    @Override
    public void onClicked(MouseButton button) {
        action.accept(this);
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        tooltip.forEach(list::add);
    }
}
