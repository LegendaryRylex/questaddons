package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftbquests.client.FTBQuestsKeyMappings;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenArrowMoveMixin {
    @ModifyExpressionValue(
            method = "keyPressed",
            at = @At(value = "FIELD", target = "Ldev/ftb/mods/ftbquests/client/gui/quests/QuestScreen;movingObjects:Z"))
    private boolean questaddons$arrowMoveSelection(boolean original, @Local(argsOnly = true) Key key) {
        if (original || !isArrow(key)) {
            return original;
        }
        QuestScreen self = (QuestScreen) (Object) this;
        return self.getViewedQuest() == null
                && !((QuestScreenAccessor) this).questaddons$selectedObjects().isEmpty();
    }

    private static boolean isArrow(Key key) {
        return key.matches(FTBQuestsKeyMappings.KEY_GUI_UP)
                || key.matches(FTBQuestsKeyMappings.KEY_GUI_DOWN)
                || key.matches(FTBQuestsKeyMappings.KEY_GUI_LEFT)
                || key.matches(FTBQuestsKeyMappings.KEY_GUI_RIGHT);
    }
}
