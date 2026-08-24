package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.QuestAddonsKeys;
import dev.rylex.questaddons.client.SmartFilterFromQuest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenFilterMixin {
    @Shadow
    public abstract Quest getViewedQuest();

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void questaddons$disarmGestureGuard(Key key, CallbackInfoReturnable<Boolean> cir) {
        ClickGestureGuard.disarm();
    }

    @Inject(method = "keyPressed", at = @At("RETURN"), cancellable = true)
    private void questaddons$smartFilterFromQuest(Key key, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !key.matches(QuestAddonsKeys.QUEST_FILTER)) {
            return;
        }

        Quest quest = getViewedQuest();
        if (quest != null && SmartFilterFromQuest.give(quest)) {
            cir.setReturnValue(true);
        }
    }
}
