package dev.rylex.questaddons.compat.jei;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class JeiCompat {
    public static final String MOD_ID = "jei";

    private JeiCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isJeiScreen(Screen screen) {
        return QuestAddonsJeiPlugin.isJeiScreen(screen);
    }

    public static void showRecipes(ItemStack stack, boolean uses) {
        QuestAddonsJeiPlugin.showRecipes(stack, uses);
    }
}
