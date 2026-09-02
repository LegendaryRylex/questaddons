package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.rylex.questaddons.client.SplitView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
}
