package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.rylex.questaddons.client.DescriptionEditor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen$OuterPanel", remap = false)
public abstract class MultilineTextEditorOuterPanelMixin extends Panel {
    private MultilineTextEditorOuterPanelMixin(Panel panel) {
        super(panel);
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    private void questaddons$addPreview(CallbackInfo ci) {
        ((DescriptionEditor) getGui()).questaddons$previewWidgets().forEach(this::add);
    }
}
