package dev.rylex.questboxselect.mixin;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questboxselect.client.BoxSelectKeys;
import dev.rylex.questboxselect.client.BoxSelectState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuestPanel.class, remap = false)
public abstract class QuestPanelMixin {
    @Shadow
    @Final
    private QuestScreen questScreen;

    @Inject(method = "mousePressed", at = @At("RETURN"))
    private void questboxselect$beginBoxSelect(MouseButton button, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !button.isLeft() || !BoxSelectKeys.isBoxSelectHeld()) {
            return;
        }
        if (((QuestScreenAccessor) questScreen).questboxselect$grabbed() != button) {
            return;
        }
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        BoxSelectState.active = file != null && file.canEdit();
    }

    @Inject(method = "mouseReleased", at = @At("RETURN"))
    private void questboxselect$endBoxSelect(MouseButton button, CallbackInfo ci) {
        if (!BoxSelectState.active || !button.isLeft()) {
            return;
        }
        BoxSelectState.active = false;

        Panel self = (Panel) (Object) this;
        ((QuestScreenAccessor) questScreen)
                .questboxselect$selectAllQuestsInBox(
                        self.getMouseX(), self.getMouseY(), self.getScrollX(), self.getScrollY());
    }
}
