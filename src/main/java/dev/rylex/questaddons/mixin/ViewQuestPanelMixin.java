package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.ui.BlankPanel;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.FloatingQuestPanel;
import dev.rylex.questaddons.client.PreviewTargets;
import dev.rylex.questaddons.client.QuestWindows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class ViewQuestPanelMixin {
    @Shadow
    private Quest quest;

    @Shadow
    private BlankPanel panelContent;

    @ModifyVariable(method = "editDescription", at = @At("STORE"), ordinal = 0)
    private ListConfig<String, StringConfig> questaddons$trackDescription(ListConfig<String, StringConfig> config) {
        PreviewTargets.register(config, quest, ((Widget) (Object) this).width);
        return config;
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$keysOnlyWhenHovered(Key key, CallbackInfo ci) {
        if ((Object) this instanceof FloatingQuestPanel floating && !floating.isMouseOver()) {
            ci.cancel();
        }
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$swallowGestureRelease(Key key, CallbackInfo ci) {
        if (!ClickGestureGuard.isArmed() || !key.is(ClickGestureGuard.armedKey())) {
            return;
        }

        ClickGestureGuard.disarm();
        ci.cancel();
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void questaddons$scrollOnlyWhenHovered(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof FloatingQuestPanel floating && !floating.isMouseOver()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mousePressed", at = @At("HEAD"), cancellable = true)
    private void questaddons$grabTitleBar(MouseButton button, CallbackInfoReturnable<Boolean> cir) {
        ViewQuestPanel self = (ViewQuestPanel) (Object) this;
        if (quest == null || panelContent == null || !self.isMouseOver()) {
            return;
        }

        if (self instanceof FloatingQuestPanel floating) {
            QuestWindows.raise(floating);
        }

        if (!button.isLeft() || self.getMouseY() >= panelContent.getY()) {
            return;
        }

        for (Widget widget : self.getWidgets()) {
            if (widget instanceof Button && widget.isMouseOver()) {
                return;
            }
        }

        QuestWindows.beginDrag(self, self.getMouseX(), self.getMouseY());
        cir.setReturnValue(true);
    }

}
