package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.ExpandChaptersButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.DependencyHighlight;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExpandChaptersButton.class, remap = false)
public abstract class ExpandChaptersButtonMixin {
    private static final int ARROW_SIZE = 12;

    @Shadow
    @Final
    private QuestScreen questScreen;

    @Inject(method = "draw", at = @At("TAIL"))
    private void questaddons$outlineHiddenChapterList(
            GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        ChapterPanelAccessor chapters =
                (ChapterPanelAccessor) ((QuestScreenAccessor) questScreen).questaddons$chapterPanel();
        if (!chapters.questaddons$expanded() && DependencyHighlight.hasOtherChapter(questScreen)) {
            DependencyHighlight.outlineAround(
                    graphics, x + (w - ARROW_SIZE) / 2, y + (h - ARROW_SIZE) / 2, ARROW_SIZE, ARROW_SIZE);
        }
    }
}
