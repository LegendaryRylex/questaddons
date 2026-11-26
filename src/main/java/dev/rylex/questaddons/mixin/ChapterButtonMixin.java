package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.rylex.questaddons.client.DependencyHighlight;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChapterPanel.ChapterButton.class, remap = false)
public abstract class ChapterButtonMixin {
    @Shadow
    @Final
    private Chapter chapter;

    @Inject(method = "draw", at = @At("TAIL"))
    private void questaddons$outlineDependencyChapter(
            GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        if (((Widget) (Object) this).getGui() instanceof QuestScreen screen
                && DependencyHighlight.isChapter(screen, chapter)) {
            DependencyHighlight.outlineInside(graphics, x + 1, y, w - 2, h);
        }
    }
}
