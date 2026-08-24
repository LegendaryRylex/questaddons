package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Key.class, remap = false)
public abstract class KeyMixin {
    @Shadow
    public abstract boolean is(int k);

    @ModifyReturnValue(method = "enter", at = @At("RETURN"))
    private boolean questaddons$numpadEnterCountsAsEnter(boolean original) {
        return original || is(GLFW.GLFW_KEY_KP_ENTER);
    }
}
