package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.client.BoxSelectState;
import dev.rylex.questaddons.client.GridSnap;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenMixin {
    @ModifyExpressionValue(
            method = "drawBackground",
            at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftblibrary/ui/input/MouseButton;isLeft()Z"))
    private boolean questaddons$suppressPan(boolean original) {
        return original && !BoxSelectState.active;
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void questaddons$drawSelectionBox(
            GuiGraphics graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        QuestScreenAccessor accessor = (QuestScreenAccessor) this;
        if (accessor.questaddons$grabbed() == null) {
            BoxSelectState.active = false;
            return;
        }
        if (!BoxSelectState.active) {
            return;
        }

        BaseScreen self = (BaseScreen) (Object) this;
        int mouseX = self.getMouseX();
        int mouseY = self.getMouseY();
        int prevX = accessor.questaddons$prevMouseX();
        int prevY = accessor.questaddons$prevMouseY();

        int boxX = Math.min(prevX, mouseX);
        int boxY = Math.min(prevY, mouseY);
        int boxW = Math.abs(mouseX - prevX);
        int boxH = Math.abs(mouseY - prevY);

        GuiHelper.drawHollowRect(graphics, boxX, boxY, boxW, boxH, Color4I.DARK_GRAY, false);
        Color4I.DARK_GRAY.withAlpha(40).draw(graphics, boxX, boxY, boxW, boxH);
    }

    @ModifyVariable(method = "getSnappedXY", at = @At("STORE"), name = "snap")
    private double questaddons$quarterGridSnap(double original) {
        return GridSnap.SNAP;
    }
}
