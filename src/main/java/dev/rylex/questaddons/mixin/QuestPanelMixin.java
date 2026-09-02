package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.BoxSelectState;
import dev.rylex.questaddons.client.GridSnap;
import dev.rylex.questaddons.client.QuestAddonsKeys;
import org.objectweb.asm.Opcodes;
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
        ClientQuestFile file = ClientQuestFile.INSTANCE;
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

    @ModifyExpressionValue(
            method = "draw",
            at =
                    @At(
                            value = "FIELD",
                            target = "Ldev/ftb/mods/ftbquests/client/gui/quests/QuestScreen;width:I",
                            opcode = Opcodes.GETFIELD))
    private int questaddons$centreOnOwningScreenX(int original) {
        return original + 2 * questScreen.getX();
    }

    @ModifyExpressionValue(
            method = "draw",
            at =
                    @At(
                            value = "FIELD",
                            target = "Ldev/ftb/mods/ftbquests/client/gui/quests/QuestScreen;height:I",
                            opcode = Opcodes.GETFIELD))
    private int questaddons$centreOnOwningScreenY(int original) {
        return original + 2 * questScreen.getY();
    }

    @ModifyVariable(method = "draw", at = @At("STORE"), name = "snap")
    private double questaddons$quarterGridSnap(double original) {
        return GridSnap.SNAP;
    }
}
