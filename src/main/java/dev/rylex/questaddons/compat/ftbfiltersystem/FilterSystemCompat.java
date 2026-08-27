package dev.rylex.questaddons.compat.ftbfiltersystem;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class FilterSystemCompat {
    public static final String MOD_ID = "ftbfiltersystem";

    private static final ResourceLocation SMART_FILTER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "smart_filter");
    private static final ResourceLocation FILTER_STRING = ResourceLocation.fromNamespaceAndPath(MOD_ID, "filter");

    private FilterSystemCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isSmartFilter(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(SMART_FILTER);
    }

    public static Optional<ItemStack> orFilterOf(Collection<ResourceLocation> itemIds) {
        if (itemIds.isEmpty()
                || !BuiltInRegistries.ITEM.containsKey(SMART_FILTER)
                || !BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(FILTER_STRING)) {
            return Optional.empty();
        }

        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(SMART_FILTER));
        stack.set(
                filterComponent(),
                FilterSyntax.or(itemIds.stream().map(ResourceLocation::toString).toList()));
        return Optional.of(stack);
    }

    @SuppressWarnings("unchecked")
    private static DataComponentType<String> filterComponent() {
        return (DataComponentType<String>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(FILTER_STRING);
    }
}
