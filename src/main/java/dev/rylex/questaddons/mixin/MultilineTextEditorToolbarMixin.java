package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.rylex.questaddons.client.JsonFixHost;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
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
    private SimpleTextButton questaddons$fixJsonButton;

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
    }

    @ModifyConstant(method = "alignWidgets", constant = @Constant(intValue = 223))
    private int questaddons$shiftUndoButton(int x) {
        return x + 16;
    }

    @Inject(method = "alignWidgets", at = @At("TAIL"))
    private void questaddons$placeFixJsonButton(CallbackInfo ci) {
        if (questaddons$fixJsonButton != null) {
            questaddons$fixJsonButton.setPosAndSize(FIX_JSON_X, 1, 16, 16);
        }
    }
}
