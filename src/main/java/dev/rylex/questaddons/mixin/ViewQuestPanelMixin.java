package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.config.editable.EditableList;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableString;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.PreviewTargets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class ViewQuestPanelMixin {
    @Shadow
    private Quest quest;

    @ModifyVariable(method = "editDescription", at = @At("STORE"), ordinal = 0)
    private EditableList<String, EditableString> questaddons$trackDescription(
            EditableList<String, EditableString> config) {
        PreviewTargets.register(config, quest, ((Widget) (Object) this).width);
        return config;
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$swallowGestureRelease(Key key, CallbackInfoReturnable<Boolean> cir) {
        if (!ClickGestureGuard.isArmed() || !key.is(ClickGestureGuard.armedKey())) {
            return;
        }

        ClickGestureGuard.disarm();
        cir.setReturnValue(true);
    }
}
