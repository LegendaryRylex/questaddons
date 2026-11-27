package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.QuestSearchBar;
import dev.rylex.questaddons.client.QuestSearchOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenSearchMixin {
    @Inject(method = "openQuestSelectionGUI", at = @At("HEAD"), cancellable = true)
    private void questaddons$openSearchOverlay(CallbackInfo ci) {
        QuestSearchOverlay.open((QuestScreen) (Object) this);
        ci.cancel();
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    private void questaddons$addSearchBar(CallbackInfo ci) {
        QuestScreen screen = (QuestScreen) (Object) this;
        screen.add(new QuestSearchBar(screen));
    }

    @Inject(method = "onClosed", at = @At("HEAD"))
    private void questaddons$forgetSearchOverlay(CallbackInfo ci) {
        QuestSearchOverlay.forget((QuestScreen) (Object) this);
    }
}
