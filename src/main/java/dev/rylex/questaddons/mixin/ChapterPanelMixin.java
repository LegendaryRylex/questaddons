package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * ChapterPanel animates its slide-out by overriding getX with an absolute coordinate, so a screen
 * that does not start at x=0 renders and hit-tests its chapter list outside itself.
 */
@Mixin(value = ChapterPanel.class, remap = false)
public abstract class ChapterPanelMixin {
    @ModifyReturnValue(method = "getX", at = @At("RETURN"))
    private int questaddons$offsetToOwningScreen(int original) {
        return original + ((Widget) (Object) this).getParent().getX();
    }
}
