package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterImageButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.ChapterImage;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.QuestAddonsKeys;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChapterImageButton.class, remap = false)
public abstract class ChapterImageButtonMixin {
    @Shadow
    @Final
    private QuestScreen questScreen;

    @Shadow
    @Final
    private ChapterImage chapterImage;

    @Unique
    private boolean questaddons$consumedClick;

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$grabSelection(MouseButton button, CallbackInfo ci) {
        if (!button.isLeft() || !QuestAddonsKeys.isMoveSelectionHeld()) {
            return;
        }

        ClientQuestFile file = ClientQuestFile.getInstance();
        if (file == null || !file.canEdit()) {
            return;
        }

        QuestScreenAccessor accessor = (QuestScreenAccessor) questScreen;
        if (!accessor.questaddons$selectedObjects().contains(chapterImage)) {
            questScreen.toggleSelected(chapterImage);
        }

        ((Widget) (Object) this).playClickSound();
        accessor.questaddons$setMovingObjects(true);
        ClickGestureGuard.arm(QuestAddonsKeys.MOVE_SELECTION);
        questaddons$consumedClick = true;
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
        file.deleteObjects(List.of(chapterImage.getId()));
        questaddons$consumedClick = true;
        ci.cancel();
    }

    @ModifyReturnValue(method = "mousePressed", at = @At("RETURN"))
    private boolean questaddons$consumeGestureClick(boolean original) {
        boolean consumed = questaddons$consumedClick;
        questaddons$consumedClick = false;
        return consumed || original;
    }
}
