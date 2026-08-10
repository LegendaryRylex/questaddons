package dev.rylex.questboxselect.mixin;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.rylex.questboxselect.client.BoxSelectKeys;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestButton.class, remap = false)
public abstract class QuestButtonMixin {
    @Shadow
    @Final
    protected QuestScreen questScreen;

    @Shadow
    public abstract Movable moveAndDeleteFocus();

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void questboxselect$grabSelection(MouseButton button, CallbackInfo ci) {
        if (!button.isLeft() || !BoxSelectKeys.isMoveSelectionHeld()) {
            return;
        }

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null || !file.canEdit()) {
            return;
        }

        Movable focus = moveAndDeleteFocus();
        QuestScreenAccessor accessor = (QuestScreenAccessor) questScreen;
        if (!accessor.questboxselect$selectedObjects().contains(focus)) {
            questScreen.toggleSelected(focus);
        }

        ((Widget) (Object) this).playClickSound();
        accessor.questboxselect$setMovingObjects(true);
        ci.cancel();
    }
}
