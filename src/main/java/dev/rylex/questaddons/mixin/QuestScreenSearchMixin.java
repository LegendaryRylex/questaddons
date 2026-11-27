package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftbquests.client.config.EditableQuestObject;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.rylex.questaddons.client.QuestSearchScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenSearchMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "openQuestSelectionGUI",
            at = @At(value = "NEW", target = "dev/ftb/mods/ftbquests/client/gui/SelectQuestObjectScreen"))
    private SelectQuestObjectScreen<?> questaddons$searchScreen(
            EditableQuestObject<?> config, ConfigCallback callback, Operation<SelectQuestObjectScreen<?>> original) {
        QuestScreen screen = (QuestScreen) (Object) this;
        EditableQuestObject<QuestObjectBase> typed = (EditableQuestObject<QuestObjectBase>) config;
        ConfigCallback centred = accepted -> {
            callback.save(accepted);
            if (accepted && typed.getValue() instanceof Movable movable) {
                screen.questPanel.scrollTo(movable.getX(), movable.getY());
            }
        };
        return new QuestSearchScreen(typed, centred, ((QuestScreenAccessor) screen).questaddons$selectedChapter());
    }
}
