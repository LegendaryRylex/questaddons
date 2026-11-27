package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.rylex.questaddons.client.ItemRecipeClick;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemTask.class, remap = false)
public abstract class ItemTaskMixin {
    @Inject(method = "onButtonClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$showBucketFluid(Button button, boolean canClick, CallbackInfo ci) {
        ItemTask self = (ItemTask) (Object) this;
        if (self.consumesResources()) {
            return;
        }
        List<ItemStack> items = self.getValidDisplayItems();
        if (items.size() != 1) {
            return;
        }
        ItemStack stack = items.getFirst();
        Optional<FluidStack> fluid = ItemRecipeClick.bucketFluid(stack);
        if (fluid.isEmpty()) {
            return;
        }
        button.playClickSound();
        ItemRecipeClick.showFluidOrItem(fluid.get(), stack);
        ci.cancel();
    }
}
