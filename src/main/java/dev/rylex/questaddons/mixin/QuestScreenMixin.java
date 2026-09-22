package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.GridSnap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenMixin {
    @ModifyVariable(method = "getSnappedXY", at = @At("STORE"), name = "snap")
    private double questaddons$gridSnap(double original) {
        return GridSnap.SNAP;
    }
}
