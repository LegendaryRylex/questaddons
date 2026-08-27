package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import java.util.Objects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenSelectionMixin {
    /**
     * FTB Quests restores a persisted selection by filtering ids on {@code getBase} but resolving
     * them with {@code get}, which is null for a chapter image, and {@code QuestPanel.draw}
     * dereferences every selected object once a frame.
     */
    @Inject(method = "restorePersistedScreenData", at = @At("RETURN"))
    private void questaddons$dropUnresolvedSelection(
            BaseQuestFile file, QuestScreen.PersistedData persistedData, CallbackInfo ci) {
        ((QuestScreenAccessor) this).questaddons$selectedObjects().removeIf(Objects::isNull);
    }
}
