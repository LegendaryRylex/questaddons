package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.rylex.questaddons.client.DescriptionEditor;
import dev.rylex.questaddons.client.DescriptionInsert;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen$ToolbarPanel", remap = false)
public abstract class MultilineTextEditorToolbarMixin extends Panel {
    @Unique
    private static final int FIRST_X = 213;

    @Unique
    private static final int SECOND_ROW_X = 27;

    @Unique
    private static final int BUTTON_SIZE = 16;

    @Unique
    private static final int ROW_HEIGHT = 18;

    @Unique
    private static final int UNDO_GAP = 10;

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
        questaddons$buttons.add(SimpleTextButton.create(
                this,
                Component.empty(),
                Icons.CHECK.withPadding(2),
                button -> ((DescriptionEditor) getGui()).questaddons$fixJson(),
                Component.translatable("questaddons.gui.fix_json"),
                Component.translatable("questaddons.gui.fix_json.tooltip"),
                Component.translatable("questaddons.gui.fix_json.hotkey", modifier)
                        .withStyle(ChatFormatting.DARK_GRAY)));
        Arrays.stream(DescriptionInsert.values())
                .filter(DescriptionInsert::isAvailable)
                .forEach(insert -> questaddons$buttons.add(SimpleTextButton.create(
                        this,
                        Component.empty(),
                        insert.icon().withPadding(2),
                        button -> ((DescriptionEditor) getGui()).questaddons$insert(insert),
                        insert.title(),
                        insert.tooltip())));
        questaddons$buttons.forEach(this::add);
    }

    @ModifyConstant(method = "alignWidgets", constant = @Constant(intValue = 223))
    private int questaddons$shiftUndoButton(int x) {
        return questaddons$fitsOneRow() ? questaddons$undoX() : x;
    }

    @Inject(method = "alignWidgets", at = @At("TAIL"))
    private void questaddons$placeButtons(CallbackInfo ci) {
        boolean oneRow = questaddons$fitsOneRow();
        int x = oneRow ? FIRST_X : SECOND_ROW_X;
        int y = oneRow ? 1 : ROW_HEIGHT + 1;
        for (SimpleTextButton button : questaddons$buttons) {
            button.setPosAndSize(x, y, BUTTON_SIZE, BUTTON_SIZE);
            x += BUTTON_SIZE;
        }
        if (!oneRow) {
            setHeight(ROW_HEIGHT * 2);
        }
    }

    @Unique
    private int questaddons$undoX() {
        return FIRST_X + questaddons$buttons.size() * BUTTON_SIZE + UNDO_GAP;
    }

    @Unique
    private boolean questaddons$fitsOneRow() {
        return questaddons$undoX() + BUTTON_SIZE + CANCEL_ROOM <= width;
    }
}
