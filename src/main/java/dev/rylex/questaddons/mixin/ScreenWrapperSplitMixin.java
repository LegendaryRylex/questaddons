package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.ScreenWrapper;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.rylex.questaddons.client.SplitView;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    public abstract BaseScreen getGui();

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$splitMouseClicked(
            MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.mouseClicked(getGui(), event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$splitMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.mouseReleased(getGui(), event.x(), event.y(), event.button())) {
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
            MouseButtonEvent event, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.mouseDragged(getGui(), event.x(), event.y(), event.button(), dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void questaddons$splitCharTyped(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.charTyped(getGui(), event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "extractRenderState",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftblibrary/client/gui/widget/BaseScreen;addMouseOverText(Ldev/ftb/mods/ftblibrary/util/TooltipList;)V",
                            shift = At.Shift.AFTER))
    private void questaddons$splitPaneTooltip(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SplitView.addPaneTooltip(getGui(), tooltipList);
    }
}
