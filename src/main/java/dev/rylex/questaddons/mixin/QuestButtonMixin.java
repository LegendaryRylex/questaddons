package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.ChangeProgressMessage;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.QuestAddonsKeys;
import java.util.List;
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
    @Final
    Quest quest;

    @Shadow
    public abstract Movable moveAndDeleteFocus();

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$changeProgress(MouseButton button, CallbackInfo ci) {
        if (!button.isLeft()) {
            return;
        }

        boolean reset;
        if (QuestAddonsKeys.isInstantCompleteHeld()) {
            reset = false;
        } else if (QuestAddonsKeys.isResetProgressHeld()) {
            reset = true;
        } else {
            return;
        }

        ClientQuestFile file = ClientQuestFile.getInstance();
        if (file == null || !file.canEdit()) {
            return;
        }

        ((Widget) (Object) this).playClickSound();
        ClickGestureGuard.arm(reset ? QuestAddonsKeys.RESET_PROGRESS : QuestAddonsKeys.INSTANT_COMPLETE);
        ChangeProgressMessage.sendToServer(
                FTBQuestsClient.getClientPlayerData(), quest, progressChange -> progressChange.setReset(reset));
        ci.cancel();
    }

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$deleteObject(MouseButton button, CallbackInfo ci) {
        if (!button.isLeft() || !QuestAddonsKeys.isDeleteObjectHeld()) {
            return;
        }

        ClientQuestFile file = ClientQuestFile.getInstance();
        if (file == null || !file.canEdit()) {
            return;
        }

        ((Widget) (Object) this).playClickSound();
        ClickGestureGuard.arm(QuestAddonsKeys.DELETE_OBJECT);
        file.deleteObjects(List.of(moveAndDeleteFocus().getMovableID()));
        ci.cancel();
    }

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$grabSelection(MouseButton button, CallbackInfo ci) {
        if (!button.isLeft() || !QuestAddonsKeys.isMoveSelectionHeld()) {
            return;
        }

        ClientQuestFile file = ClientQuestFile.getInstance();
        if (file == null || !file.canEdit()) {
            return;
        }

        Movable focus = moveAndDeleteFocus();
        QuestScreenAccessor accessor = (QuestScreenAccessor) questScreen;
        if (!accessor.questaddons$selectedObjects().contains(focus)) {
            questScreen.toggleSelected(focus);
        }

        ((Widget) (Object) this).playClickSound();
        ClickGestureGuard.arm(QuestAddonsKeys.MOVE_SELECTION);
        accessor.questaddons$setMovingObjects(true);
        ci.cancel();
    }
}
