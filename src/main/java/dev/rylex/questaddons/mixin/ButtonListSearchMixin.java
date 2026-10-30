package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.rylex.questaddons.client.QuestObjectHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractButtonListScreen.class, remap = false)
public abstract class ButtonListSearchMixin {
    @Inject(method = "getFilterText", at = @At("RETURN"), cancellable = true)
    private void questaddons$searchById(Widget widget, CallbackInfoReturnable<String> cir) {
        if (widget instanceof QuestObjectHolder holder) {
            QuestObjectBase object = holder.questaddons$object();
            if (object != null) {
                String id = object.getCodeString().toLowerCase();
                cir.setReturnValue(cir.getReturnValue() + "\n" + id + "\n#" + id);
            }
        }
    }
}
