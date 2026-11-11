package dev.rylex.questaddons.client;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public final class ItemRecipeClick {
    private ItemRecipeClick() {}

    public static boolean show(Style style) {
        if (style == null || style.getClickEvent() != null || style.getHoverEvent() == null) {
            return false;
        }

        HoverEvent.ItemStackInfo item = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ITEM);
        RecipeModHelper helper = FTBQuests.getRecipeModHelper();
        if (item == null || helper instanceof RecipeModHelper.NoOp) {
            return false;
        }

        helper.showRecipes(item.getItemStack());
        return true;
    }
}
