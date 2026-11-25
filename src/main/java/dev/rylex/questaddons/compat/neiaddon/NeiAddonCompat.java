package dev.rylex.questaddons.compat.neiaddon;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class NeiAddonCompat {
    public static final String MOD_ID = "neiaddon";

    private NeiAddonCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static boolean keyPressed(ItemStack stack, long amount, int keyCode, boolean allowControl) {
        return NeiItemKeys.keyPressed(stack, amount, keyCode, allowControl);
    }
}
