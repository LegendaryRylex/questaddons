package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.rylex.questaddons.client.JsonFixHost;
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

    /** Finds convert and undo by position, the last two left-aligned buttons, since FTB Quests moves them between releases. */
    @Inject(method = "alignWidgets", at = @At("TAIL"))
    private void questaddons$placeFixJsonButton(CallbackInfo ci) {
        if (questaddons$fixJsonButton == null) {
            return;
        }

        List<Widget> leftAligned = getWidgets().stream()
                .filter(widget -> widget != questaddons$fixJsonButton && widget.getPosX() < width - 17)
                .sorted(Comparator.comparingInt(Widget::getPosX))
                .toList();
        if (leftAligned.size() < 2) {
            return;
        }

        Widget convert = leftAligned.get(leftAligned.size() - 2);
        Widget undo = leftAligned.getLast();
        questaddons$fixJsonButton.setPosAndSize(convert.getPosX() + 16, 1, 16, 16);
        undo.setPos(undo.getPosX() + 16, undo.getPosY());
    }
}
