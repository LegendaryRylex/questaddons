package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.PinnedQuestsTracker;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.rylex.questaddons.client.pin.PinnedTracker;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PinnedQuestsTracker.class, remap = false)
public abstract class PinnedQuestsTrackerMixin {
    @Shadow
    private boolean showChapterTitle;

    @Inject(method = "collectPinnedQuests", at = @At("HEAD"))
    private void questaddons$clearTracker(ClientQuestFile file, CallbackInfo ci) {
        PinnedTracker.get().clear();
    }

    @Inject(method = "rebuildPinnedText", at = @At("HEAD"))
    private void questaddons$rebuildTracker(List<Quest> pinned, Minecraft minecraft, TeamData data, CallbackInfo ci) {
        PinnedTracker.get().rebuild(pinned, data, showChapterTitle);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void questaddons$renderTracker(Minecraft minecraft, GuiGraphics graphics, CallbackInfo ci) {
        PinnedTracker.get().renderHud(minecraft, graphics);
        ci.cancel();
    }
}
