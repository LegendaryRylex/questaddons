package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.rylex.questaddons.client.QuestWindows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BaseScreen.class, remap = false)
public abstract class BaseScreenQuestWindowsMixin {
    @Inject(method = "mousePressed", at = @At("HEAD"))
    private void questaddons$dropStaleWindowDrag(MouseButton button, CallbackInfoReturnable<Boolean> cir) {
        QuestWindows.endDrag();
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void questaddons$endWindowDrag(MouseButton button, CallbackInfo ci) {
        if (button.isLeft()) {
            QuestWindows.endDrag();
        }
    }
}
