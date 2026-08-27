package dev.rylex.questaddons.client;

import dev.rylex.questaddons.compat.ftbfiltersystem.FilterSystemCompat;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class SmartFilterFromInventory {
    private SmartFilterFromInventory() {}

    public static boolean give(boolean wholeInventory) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        return SmartFilter.give(
                itemIds(player.getInventory(), wholeInventory),
                wholeInventory
                        ? "questaddons.smart_filter.no_inventory_items"
                        : "questaddons.smart_filter.no_hotbar_items");
    }

    private static Set<ResourceLocation> itemIds(Inventory inventory, boolean wholeInventory) {
        Set<ResourceLocation> itemIds = new LinkedHashSet<>();
        int slots = wholeInventory ? Inventory.INVENTORY_SIZE : Inventory.getSelectionSize();
        for (int slot = 0; slot < slots; slot++) {
            add(itemIds, inventory.getItem(slot));
        }
        if (wholeInventory) {
            add(itemIds, inventory.getItem(Inventory.SLOT_OFFHAND));
        }
        return itemIds;
    }

    private static void add(Set<ResourceLocation> itemIds, ItemStack stack) {
        if (!stack.isEmpty() && !FilterSystemCompat.isSmartFilter(stack)) {
            itemIds.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
    }
}
