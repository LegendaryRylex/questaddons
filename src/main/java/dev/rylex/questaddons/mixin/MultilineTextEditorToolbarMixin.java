package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.rylex.questaddons.client.DescriptionEditor;
import dev.rylex.questaddons.client.DescriptionInsert;
import dev.rylex.questaddons.client.EditorButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen$ToolbarPanel", remap = false)
public abstract class MultilineTextEditorToolbarMixin extends Panel {
    @Unique
    private static final int SECOND_ROW_X = 27;

    @Unique
    private static final int BUTTON_SIZE = 16;

    @Unique
    private static final int ROW_HEIGHT = 18;

    @Unique
    private static final int CANCEL_ROOM = 21;

    @Unique
    private final List<SimpleTextButton> questaddons$buttons = new ArrayList<>();

    private MultilineTextEditorToolbarMixin(Panel panel) {
        super(panel);
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    private void questaddons$addButtons(CallbackInfo ci) {
        questaddons$buttons.clear();
        String modifier = Util.getPlatform() == Util.OS.OSX ? "Ctrl" : "Alt";
        questaddons$buttons.add(new EditorButton(
                this,
                Icons.CHECK.withPadding(2),
                button -> ((DescriptionEditor) getGui()).questaddons$fixJson(),
                Component.translatable("questaddons.gui.fix_json"),
                Component.translatable("questaddons.gui.fix_json.tooltip"),
                Component.translatable("questaddons.gui.fix_json.hotkey", modifier)
                        .withStyle(ChatFormatting.DARK_GRAY)));
        Arrays.stream(DescriptionInsert.values())
                .filter(DescriptionInsert::isAvailable)
                .forEach(insert -> questaddons$buttons.add(new EditorButton(
                        this,
                        insert.icon().withPadding(2),
                        button -> ((DescriptionEditor) getGui())
                                .questaddons$insert(insert, button.getX(), button.getY() + button.height),
                        insert.title(),
                        insert.tooltip())));
        questaddons$buttons.forEach(this::add);
    }

    /** Finds convert and undo by position, the last two left-aligned buttons, since FTB Quests moves them between releases. */
    @Inject(method = "alignWidgets", at = @At("TAIL"))
    private void questaddons$placeButtons(CallbackInfo ci) {
        List<Widget> leftAligned = getWidgets().stream()
                .filter(widget -> !questaddons$buttons.contains(widget) && widget.getPosX() < width - 17)
                .sorted(Comparator.comparingInt(Widget::getPosX))
                .toList();
        if (questaddons$buttons.isEmpty() || leftAligned.size() < 2) {
            return;
        }

        Widget convert = leftAligned.get(leftAligned.size() - 2);
        Widget undo = leftAligned.getLast();
        int shift = questaddons$buttons.size() * BUTTON_SIZE;
        boolean oneRow = undo.getPosX() + shift + BUTTON_SIZE + CANCEL_ROOM <= width;
        int x = oneRow ? convert.getPosX() + BUTTON_SIZE : SECOND_ROW_X;
        int y = oneRow ? 1 : ROW_HEIGHT + 1;
        for (SimpleTextButton button : questaddons$buttons) {
            button.setPosAndSize(x, y, BUTTON_SIZE, BUTTON_SIZE);
            x += BUTTON_SIZE;
        }
        if (oneRow) {
            undo.setPos(undo.getPosX() + shift, undo.getPosY());
        } else {
            setHeight(ROW_HEIGHT * 2);
        }
    }
}
