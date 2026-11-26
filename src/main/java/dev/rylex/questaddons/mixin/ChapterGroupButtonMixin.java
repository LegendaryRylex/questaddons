package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.DependencyHighlight;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChapterPanel.ChapterGroupButton.class, remap = false)
public abstract class ChapterGroupButtonMixin {
    @Inject(method = "draw", at = @At("TAIL"))
    private void questaddons$outlineCollapsedGroup(
            GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        ChapterPanel.ChapterGroupButton self = (ChapterPanel.ChapterGroupButton) (Object) this;
        if (((Widget) self).getGui() instanceof QuestScreen screen
                && DependencyHighlight.isCollapsedGroup(screen, self.group)) {
            DependencyHighlight.outlineInside(graphics, x + 1, y, w - 2, h);
        }
    }
}
