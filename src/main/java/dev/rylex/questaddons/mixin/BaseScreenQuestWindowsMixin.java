package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.QuestWindows;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BaseScreen.class, remap = false)
public abstract class BaseScreenQuestWindowsMixin {
    @Inject(method = "closeGui(Z)V", at = @At("TAIL"))
    private void questaddons$forgetWindowsOnExit(boolean openPrevScreen, CallbackInfo ci) {
        if ((Object) this instanceof QuestScreen screen) {
            QuestWindows.forget(screen);
        }
    }

    @WrapOperation(
            method = {"draw", "drawForeground", "drawModalPanels"},
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftblibrary/icon/Color4I;draw(Lnet/minecraft/client/gui/GuiGraphics;IIII)V"))
    private void questaddons$skipModalDimUnderFloatingWindows(
            Color4I color, GuiGraphics graphics, int x, int y, int w, int h, Operation<Void> original) {
        if (!QuestWindows.hasFloating((BaseScreen) (Object) this)) {
            original.call(color, graphics, x, y, w, h);
        }
    }
}
