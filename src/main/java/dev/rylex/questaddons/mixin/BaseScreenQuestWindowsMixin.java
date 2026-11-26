package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.QuestWindows;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
            method = "drawModalPanels",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftblibrary/client/icon/IconHelper;renderIcon(Ldev/ftb/mods/ftblibrary/icon/Icon;Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIII)V"))
    private void questaddons$skipModalDimUnderFloatingWindows(
            Icon<?> icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h, Operation<Void> original) {
        if (!QuestWindows.hasFloating((BaseScreen) (Object) this)) {
            original.call(icon, graphics, x, y, w, h);
        }
    }
}
