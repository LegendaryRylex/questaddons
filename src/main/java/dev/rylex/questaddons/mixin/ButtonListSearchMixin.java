package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.rylex.questaddons.client.QuestObjectHolder;
import dev.rylex.questaddons.client.QuestSearchIndex;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractButtonListScreen.class, remap = false)
public abstract class ButtonListSearchMixin {
    @Shadow
    @Final
    private TextBox searchBox;

    @Inject(method = "getFilterText", at = @At("RETURN"), cancellable = true)
    private void questaddons$searchByIdAndText(Widget widget, CallbackInfoReturnable<String> cir) {
        if (widget instanceof QuestObjectHolder holder) {
            QuestObjectBase object = holder.questaddons$object();
            if (object != null) {
                String id = object.getCodeString().toLowerCase();
                String text = cir.getReturnValue() + "\n" + id + "\n#" + id;
                String query = searchBox.getText();
                if (QuestSearchIndex.matches(object, query, this)) {
                    text += "\n" + query.toLowerCase();
                }
                cir.setReturnValue(text);
            }
        }
    }
}
