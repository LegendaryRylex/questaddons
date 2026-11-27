package dev.rylex.questaddons.compat.jei;

import dev.rylex.questaddons.client.RecipeLink;
import dev.rylex.questaddons.client.RecipeTarget;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

public final class JeiCompat {
    public static final String MOD_ID = "jei";

    private JeiCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isJeiScreen(Screen screen) {
        return QuestAddonsJeiPlugin.isJeiScreen(screen);
    }

    public static boolean hasKeyboardFocus() {
        return QuestAddonsJeiPlugin.hasKeyboardFocus();
    }

    public static void showRecipes(ItemStack stack, boolean uses) {
        QuestAddonsJeiPlugin.showRecipes(stack, uses);
    }

    public static List<ItemStack> visibleItems() {
        return JeiIngredients.items();
    }

    public static List<FluidStack> visibleFluids() {
        return JeiIngredients.fluids();
    }

    public static List<RecipeTarget> recipeTargets() {
        return JeiRecipeLinks.targets();
    }

    public static List<RecipeLink> recipesMaking(RecipeTarget target) {
        return JeiRecipeLinks.making(target);
    }

    public static boolean isRecipeLink(String value) {
        return value.startsWith(JeiRecipeLinks.SCHEME);
    }

    public static boolean openRecipeLink(String value) {
        return JeiRecipeLinks.open(value);
    }
}
