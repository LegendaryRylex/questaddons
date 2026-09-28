package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.QuestAddonsKeys;
import dev.rylex.questaddons.client.SaveQuests;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenSaveMixin {
    @Inject(method = "keyPressed", at = @At("RETURN"), cancellable = true)
    private void questaddons$saveQuests(Key key, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !key.matches(QuestAddonsKeys.SAVE_QUESTS)) {
            return;
        }

        boolean handled = key.modifiers.shift() ? SaveQuests.downloadQuestFiles() : SaveQuests.saveOnServer();
        if (handled) {
            cir.setReturnValue(true);
        }
    }
}
