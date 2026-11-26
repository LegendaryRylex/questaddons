package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenSearchMixin {
    @ModifyArg(
            method = "openQuestSelectionGUI",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftbquests/client/gui/SelectQuestObjectScreen;<init>(Ldev/ftb/mods/ftbquests/util/ConfigQuestObject;Ldev/ftb/mods/ftblibrary/config/ConfigCallback;)V"),
            index = 1)
    private ConfigCallback questaddons$centreOnSearchResult(
            ConfigQuestObject<QuestObject> config, ConfigCallback callback) {
        QuestScreen screen = (QuestScreen) (Object) this;
        return accepted -> {
            callback.save(accepted);
            if (accepted && config.getValue() instanceof Movable movable) {
                screen.questPanel.scrollTo(movable.getX(), movable.getY());
            }
        };
    }
}
