package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.rylex.questaddons.client.ClickGestureGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class ViewQuestPanelMixin {
    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$swallowGestureRelease(Key key, CallbackInfo ci) {
        if (!ClickGestureGuard.isArmed() || !key.is(ClickGestureGuard.armedKey())) {
            return;
        }

        ClickGestureGuard.disarm();
        ci.cancel();
    }
}
