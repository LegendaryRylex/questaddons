package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import dev.rylex.questaddons.client.SplitView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BaseScreen.class, remap = false)
public abstract class BaseScreenSplitMixin {
    @ModifyReturnValue(method = "getX", at = @At("RETURN"))
    private int questaddons$anchorSplitRootX(int original) {
        return SplitView.rootX((BaseScreen) (Object) this, original);
    }

    @ModifyReturnValue(method = "getY", at = @At("RETURN"))
    private int questaddons$anchorSplitRootY(int original) {
        return SplitView.rootY((BaseScreen) (Object) this, original);
    }

    @Inject(method = "openGui", at = @At("HEAD"), cancellable = true)
    private void questaddons$openRootInsteadOfPane(CallbackInfo ci) {
        if (SplitView.reopenRoot((BaseScreen) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "closeGui(Z)V", at = @At("HEAD"), cancellable = true)
    private void questaddons$closeRootInsteadOfPane(boolean openPrevScreen, CallbackInfo ci) {
        if (SplitView.closeRoot((BaseScreen) (Object) this, openPrevScreen)) {
            ci.cancel();
        }
    }
}
