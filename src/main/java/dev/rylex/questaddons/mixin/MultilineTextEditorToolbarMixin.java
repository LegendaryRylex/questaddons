package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.rylex.questaddons.client.JsonFixHost;
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
    private static final int FIX_JSON_X = 213;

    @Unique
    private static final int KEYBIND_X = FIX_JSON_X + 16;

    @Unique
    private SimpleTextButton questaddons$fixJsonButton;

    @Unique
    private SimpleTextButton questaddons$keybindButton;

    private MultilineTextEditorToolbarMixin(Panel panel) {
        super(panel);
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    private void questaddons$addFixJsonButton(CallbackInfo ci) {
        String modifier = Util.getPlatform() == Util.OS.OSX ? "Ctrl" : "Alt";
        questaddons$fixJsonButton = SimpleTextButton.create(
                this,
                Component.empty(),
                Icons.CHECK.withPadding(2),
                button -> ((JsonFixHost) getGui()).questaddons$fixJson(),
                Component.translatable("questaddons.gui.fix_json"),
                Component.translatable("questaddons.gui.fix_json.tooltip"),
                Component.translatable("questaddons.gui.fix_json.hotkey", modifier)
                        .withStyle(ChatFormatting.DARK_GRAY));
        add(questaddons$fixJsonButton);

        questaddons$keybindButton = SimpleTextButton.create(
                this,
                Component.empty(),
                Icons.KEYBOARD.withPadding(2),
                button -> ((JsonFixHost) getGui()).questaddons$openKeybindMenu(),
                Component.translatable("questaddons.gui.insert_keybind"),
                Component.translatable("questaddons.gui.insert_keybind.tooltip"));
        add(questaddons$keybindButton);
    }

    @ModifyConstant(method = "alignWidgets", constant = @Constant(intValue = 223))
    private int questaddons$shiftUndoButton(int x) {
        return x + 32;
    }

    @Inject(method = "alignWidgets", at = @At("TAIL"))
    private void questaddons$placeFixJsonButton(CallbackInfo ci) {
        if (questaddons$fixJsonButton != null) {
            questaddons$fixJsonButton.setPosAndSize(FIX_JSON_X, 1, 16, 16);
            questaddons$keybindButton.setPosAndSize(KEYBIND_X, 1, 16, 16);
        }
    }
}
