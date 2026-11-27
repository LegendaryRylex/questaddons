package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
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
            ConfigQuestObject<?> config, ConfigCallback callback, Operation<SelectQuestObjectScreen<?>> original) {
        QuestScreen screen = (QuestScreen) (Object) this;
        ConfigQuestObject<QuestObjectBase> typed = (ConfigQuestObject<QuestObjectBase>) config;
        ConfigCallback centred = accepted -> {
            callback.save(accepted);
            if (accepted && typed.getValue() instanceof Movable movable) {
                screen.questPanel.scrollTo(movable.getX(), movable.getY());
            }
        };
        return new QuestSearchScreen(typed, centred, ((QuestScreenAccessor) screen).questaddons$selectedChapter());
    }
}
