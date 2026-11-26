package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.BoxSelectState;
import dev.rylex.questaddons.client.GridSnap;
import dev.rylex.questaddons.client.QuestAddonsKeys;
import dev.rylex.questaddons.client.QuestWindows;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuestPanel.class, remap = false)
public abstract class QuestPanelMixin {
    @Shadow
    @Final
    private QuestScreen questScreen;

    @Inject(method = "mousePressed", at = @At("RETURN"))
    private void questaddons$beginBoxSelect(MouseButton button, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !button.isLeft() || !QuestAddonsKeys.isBoxSelectHeld()) {
            return;
        }
        QuestScreenAccessor accessor = (QuestScreenAccessor) questScreen;
        if (accessor.questaddons$grabbed() != button || accessor.questaddons$movingObjects()) {
            return;
        }
        ClientQuestFile file = ClientQuestFile.getInstance();
        if (file != null && file.canEdit()) {
            BoxSelectState.begin(questScreen);
        }
    }

    @Inject(method = "mouseReleased", at = @At("RETURN"))
    private void questaddons$endBoxSelect(MouseButton button, CallbackInfo ci) {
        if (!BoxSelectState.isActive(questScreen) || !button.isLeft()) {
            return;
        }
        BoxSelectState.end();

        Panel self = (Panel) (Object) this;
        ((QuestScreenAccessor) questScreen)
                .questaddons$selectAllQuestsInBox(
                        self.getMouseX(), self.getMouseY(), self.getScrollX(), self.getScrollY());
    }

    @ModifyVariable(method = "draw", at = @At("STORE"), name = "snap")
    private double questaddons$gridSnap(double original) {
        return GridSnap.SNAP;
    }

    @Inject(method = "checkMouseOver", at = @At("HEAD"), cancellable = true)
    private void questaddons$coveredByFloatingWindow(int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (QuestWindows.isOverFloating(questScreen, mouseX, mouseY)) {
            cir.setReturnValue(false);
        }
    }
}
