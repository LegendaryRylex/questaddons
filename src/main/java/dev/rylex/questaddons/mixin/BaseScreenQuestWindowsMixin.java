package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.QuestWindows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BaseScreen.class, remap = false)
public abstract class BaseScreenQuestWindowsMixin {
    @Inject(method = "closeGui(Z)V", at = @At("TAIL"))
    private void questaddons$forgetWindowsOnExit(boolean openPrevScreen, CallbackInfo ci) {
        if ((Object) this instanceof QuestScreen screen) {
            QuestWindows.forget(screen);
        }
    }
}
