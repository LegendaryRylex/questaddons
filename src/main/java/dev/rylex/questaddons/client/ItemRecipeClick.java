package dev.rylex.questaddons.client;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import java.util.Optional;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.neoforged.neoforge.fluids.FluidStack;

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
            helper.showRecipes(dev.architectury.fluid.FluidStack.create(
                    fluid.get().getFluid(), fluid.get().getAmount()));
            return true;
        }

        HoverEvent.ItemStackInfo item = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ITEM);
        if (item == null) {
            return false;
        }

        helper.showRecipes(item.getItemStack());
        return true;
    }
}
