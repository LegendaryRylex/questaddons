package dev.rylex.questaddons.client;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public final class ItemRecipeClick {
    private ItemRecipeClick() {}

    public static boolean show(Style style) {
        if (style == null || style.getClickEvent() != null || style.getHoverEvent() == null) {
            return false;
        }

        RecipeModHelper helper = FTBQuests.getRecipeModHelper();
        if (helper instanceof RecipeModHelper.NoOp) {
            return false;
        }

        Optional<FluidStack> fluid = FluidLink.fluid(style);
        if (fluid.isPresent()) {
            showFluid(helper, fluid.get());
            return true;
        }

        HoverEvent.ItemStackInfo item = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ITEM);
        if (item == null) {
            return false;
        }

        helper.showRecipes(item.getItemStack());
        return true;
    }

    public static Optional<FluidStack> bucketFluid(ItemStack stack) {
        if (!(stack.getItem() instanceof BucketItem)
                || !FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
            return Optional.empty();
        }
        return FluidUtil.getFluidContained(stack);
    }

    public static void showFluidOrItem(FluidStack fluid, ItemStack fallback) {
        RecipeModHelper helper = FTBQuests.getRecipeModHelper();
        Screen before = Minecraft.getInstance().screen;
        showFluid(helper, fluid);
        if (Minecraft.getInstance().screen == before) {
            helper.showRecipes(fallback);
        }
    }

    private static void showFluid(RecipeModHelper helper, FluidStack fluid) {
        helper.showRecipes(dev.architectury.fluid.FluidStack.create(fluid.getFluid(), fluid.getAmount()));
    }
}
