package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.rylex.questaddons.client.SplitView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * These two handlers refresh the current QuestScreen's panels directly instead of going through
 * QuestScreen's refresh methods, so the pane has to be refreshed here.
 */
@Mixin(value = FTBQuestsNetClient.class, remap = false)
public abstract class NetClientSplitMixin {
    @Inject(method = "moveMovableObject", at = @At("TAIL"))
    private static void questaddons$refreshPaneAfterMove(long id, long chapter, double x, double y, CallbackInfo ci) {
        SplitView.refreshPaneQuests();
    }

    @Inject(method = "togglePinned", at = @At("TAIL"))
    private static void questaddons$refreshPanePins(long id, boolean pinned, CallbackInfo ci) {
        SplitView.refreshPaneTopButtons();
    }
}
