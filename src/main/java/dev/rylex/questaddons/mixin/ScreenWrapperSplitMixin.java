package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.rylex.questaddons.client.SplitView;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScreenWrapper.class, remap = false)
public abstract class ScreenWrapperSplitMixin {
    @Shadow
    @Final
    private TooltipList tooltipList;

    @Shadow
    public abstract dev.ftb.mods.ftblibrary.ui.BaseScreen getGui();

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$splitMouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.mouseClicked(getGui(), x, y, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$splitMouseReleased(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.mouseReleased(getGui(), x, y, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void questaddons$splitMouseScrolled(
            double x, double y, double dirX, double dirY, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.mouseScrolled(getGui(), x, y, dirX, dirY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void questaddons$splitMouseDragged(
            double x, double y, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.mouseDragged(getGui(), x, y, button, dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void questaddons$splitCharTyped(char keyChar, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.charTyped(getGui(), keyChar, modifiers)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftblibrary/ui/BaseScreen;addMouseOverText(Ldev/ftb/mods/ftblibrary/util/TooltipList;)V",
                            shift = At.Shift.AFTER))
    private void questaddons$splitPaneTooltip(
            GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SplitView.addPaneTooltip(getGui(), tooltipList);
    }
}
