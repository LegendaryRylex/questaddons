package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.PreviewTargets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class ViewQuestPanelMixin {
    @Shadow
    private Quest quest;

    @ModifyVariable(method = "editDescription", at = @At("STORE"), ordinal = 0)
    private ListConfig<String, StringConfig> questaddons$trackDescription(ListConfig<String, StringConfig> config) {
        PreviewTargets.register(config, quest, ((Widget) (Object) this).width);
        return config;
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$swallowGestureRelease(Key key, CallbackInfo ci) {
        if (!ClickGestureGuard.isArmed() || !key.is(ClickGestureGuard.armedKey())) {
            return;
        }

        ClickGestureGuard.disarm();
        ci.cancel();
    }
}
