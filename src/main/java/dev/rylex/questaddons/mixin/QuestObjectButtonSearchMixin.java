package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.rylex.questaddons.client.QuestSearchIndex;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen$QuestObjectButton", remap = false)
public abstract class QuestObjectButtonSearchMixin {
    @Shadow
    @Final
    public @Nullable QuestObjectBase object;

    @ModifyReturnValue(method = "extendedSearch", at = @At("RETURN"))
    private boolean questaddons$searchText(boolean original, String query) {
        return original || object != null && QuestSearchIndex.matches(object, query, ((Widget) (Object) this).getGui());
    }
}
