package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.rylex.questaddons.client.SplitOrientation;
import dev.rylex.questaddons.client.SplitView;
import dev.rylex.questaddons.client.SplitViewPane;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenSplitMixin {
    @Inject(method = "onInit", at = @At("RETURN"))
    private void questaddons$shrinkRoot(CallbackInfoReturnable<Boolean> cir) {
        SplitView.applyRootBounds((QuestScreen) (Object) this);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void questaddons$routeKeyPressed(Key key, CallbackInfoReturnable<Boolean> cir) {
        if (SplitView.keyPressed((QuestScreen) (Object) this, key)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("RETURN"), cancellable = true)
    private void questaddons$toggleSplitView(Key key, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }

        SplitOrientation requested = SplitView.requested(key);
        if (requested == null) {
            return;
        }

        SplitView.toggle((QuestScreen) (Object) this, requested);
        cir.setReturnValue(true);
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$routeKeyReleased(Key key, CallbackInfo ci) {
        if (SplitView.keyReleased((QuestScreen) (Object) this, key)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void questaddons$tickPane(CallbackInfo ci) {
        SplitView.tick((QuestScreen) (Object) this);
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    private void questaddons$drawSplitView(
            GuiGraphics graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        SplitView.draw((QuestScreen) (Object) this, graphics);
    }

    @Inject(
            method = "open",
            at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/client/gui/quests/QuestScreen;openGui()V"),
            cancellable = true)
    private void questaddons$keepPaneOutOfTheScreenStack(QuestObject object, boolean focus, CallbackInfo ci) {
        if ((Object) this instanceof SplitViewPane) {
            ci.cancel();
        }
    }

    @Inject(method = "onClosed", at = @At("HEAD"))
    private void questaddons$closeSplitView(CallbackInfo ci) {
        SplitView.close();
    }
}
