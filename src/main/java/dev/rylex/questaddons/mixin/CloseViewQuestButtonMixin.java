package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.rylex.questaddons.client.FloatingQuestPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel$CloseViewQuestButton", remap = false)
public abstract class CloseViewQuestButtonMixin {
    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$closeFloatingWindow(MouseButton button, CallbackInfo ci) {
        Widget self = (Widget) (Object) this;
        if (self.getParent() instanceof FloatingQuestPanel panel) {
            self.playClickSound();
            panel.close();
            ci.cancel();
        }
    }
}
