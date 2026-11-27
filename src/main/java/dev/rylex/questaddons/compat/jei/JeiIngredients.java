package dev.rylex.questaddons.compat.jei;

import java.util.Collection;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

final class JeiIngredients {
    @Nullable
    private static List<ItemStack> items;

    @Nullable
    private static List<FluidStack> fluids;

    private JeiIngredients() {}

    static void attach(IJeiRuntime runtime) {
        clear();
        runtime.getJeiHelpers().getIngredientVisibility().registerListener(new IIngredientVisibility.IListener() {
            @Override
            public <V> void onIngredientVisibilityChanged(ITypedIngredient<V> ingredient, boolean visible) {
                clear();
            }

            @Override
            public <V> void onIngredientsVisibilityChanged(
                    Collection<ITypedIngredient<V>> ingredients, boolean visible) {
                clear();
            }
        });
    }

    static void clear() {
        items = null;
        fluids = null;
    }

    static List<ItemStack> items() {
        if (items == null) {
            items = visible(VanillaTypes.ITEM_STACK);
        }
        return items == null ? List.of() : items;
    }

    static List<FluidStack> fluids() {
        if (fluids == null) {
            fluids = visible(NeoForgeTypes.FLUID_STACK);
        }
        return fluids == null ? List.of() : fluids;
    }

    @Nullable
    private static <T> List<T> visible(IIngredientType<T> type) {
        IJeiRuntime runtime = QuestAddonsJeiPlugin.runtime();
        if (runtime == null) {
            return null;
        }
        IIngredientVisibility visibility = runtime.getJeiHelpers().getIngredientVisibility();
        return runtime.getIngredientManager().getAllIngredients(type).stream()
                .filter(ingredient -> visibility.isIngredientVisible(type, ingredient))
                .toList();
    }
}
