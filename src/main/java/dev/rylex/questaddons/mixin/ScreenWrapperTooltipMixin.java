package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ModalPanel;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ScreenWrapper.class, remap = false)
public abstract class ScreenWrapperTooltipMixin {
    @Unique
    private static final int QUESTADDONS$POPUP_HEADROOM = 1000;

    @Shadow
    public abstract BaseScreen getGui();

    /** BaseScreen stacks every open modal's z-level on top of the last and popup menus add 900 more, but the tooltip only clears the largest single one. */
    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/ui/BaseScreen;getMaxZLevel()I"))
    private int questaddons$clearModalStack(int original) {
        int stacked = 10;
        for (ModalPanel panel : ((BaseScreenAccessor) getGui()).questaddons$modalPanels()) {
            stacked += panel.getExtraZlevel() + 1;
        }
        return Math.max(original, stacked + QUESTADDONS$POPUP_HEADROOM);
    }
}
