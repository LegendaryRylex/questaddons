package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.GridSnap;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = QuestPanel.class, remap = false)
public abstract class QuestPanelMixin {
    @Shadow
    @Final
    private QuestScreen questScreen;

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
    private double questaddons$gridSnap(double original) {
        return GridSnap.SNAP;
    }
}
