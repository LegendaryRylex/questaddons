package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.rylex.questaddons.client.QuestAddonsKeys;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TaskButton.class, remap = false)
public abstract class TaskButtonMixin {
    @Shadow
    @Final
    Task task;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void questaddons$toggleOptional(Key key, CallbackInfoReturnable<Boolean> cir) {
        Widget self = (Widget) (Object) this;
        if (!key.matches(QuestAddonsKeys.TOGGLE_OPTIONAL) || !self.isMouseOver()) {
            return;
        }

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null || !file.canEdit()) {
            return;
        }

        TaskAccessor accessor = (TaskAccessor) task;
        accessor.questaddons$setOptionalTask(!accessor.questaddons$isOptionalTask());
        self.playClickSound();
        EditObjectMessage.sendToServer(task);
        cir.setReturnValue(true);
    }
}
