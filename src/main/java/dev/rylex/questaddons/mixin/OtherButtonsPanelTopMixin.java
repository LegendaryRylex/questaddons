package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftbquests.client.gui.quests.OtherButtonsPanelTop;
import dev.rylex.questaddons.client.SearchTabButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OtherButtonsPanelTop.class, remap = false)
public abstract class OtherButtonsPanelTopMixin {
    @Inject(method = "addWidgets", at = @At("TAIL"))
    private void questaddons$addSearchButton(CallbackInfo ci) {
        Panel panel = (Panel) (Object) this;
        panel.add(new SearchTabButton(panel));
    }
}
