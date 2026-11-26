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
        if (style == null || style.getClickEvent() != null) {
            return false;
        }

        RecipeModHelper helper = FTBQuests.getRecipeModHelper();
        if (helper instanceof RecipeModHelper.NoOp) {
            return false;
        }

        Optional<FluidStack> fluid = FluidLink.fluid(style);
        if (fluid.isPresent()) {
            helper.showRecipes(new dev.ftb.mods.ftblibrary.platform.fluid.FluidStack(
                    fluid.get().getFluid(), (long) fluid.get().getAmount()));
            return true;
        }

        if (!(style.getHoverEvent() instanceof HoverEvent.ShowItem(var item))) {
            return false;
        }

        helper.showRecipes(item.create());
        return true;
    }
}
