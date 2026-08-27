package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.compat.ftbfiltersystem.FilterSystemCompat;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class SmartFilter {
    private static final int MENU_HOTBAR_OFFSET = 36;

    private SmartFilter() {}

    public static boolean give(Set<Identifier> itemIds, String noItemsKey) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gameMode == null) {
            return false;
        }

        if (!FilterSystemCompat.isLoaded()) {
            return failed("questaddons.smart_filter.mod_missing");
        }
        if (!minecraft.gameMode.getPlayerMode().isCreative()) {
            return failed("questaddons.smart_filter.creative_only");
        }
        if (itemIds.isEmpty()) {
            return failed(noItemsKey);
        }

        Optional<ItemStack> filter = FilterSystemCompat.orFilterOf(itemIds);
        if (filter.isEmpty()) {
            return failed("questaddons.smart_filter.mod_missing");
        }

        ItemStack stack = filter.get();
        Inventory inventory = player.getInventory();
        int slot = inventory.getFreeSlot();
        if (slot < 0) {
            return failed("questaddons.smart_filter.inventory_full");
        }

        inventory.setItem(slot, stack);
        minecraft.gameMode.handleCreativeModeItemAdd(
                stack, Inventory.isHotbarSlot(slot) ? slot + MENU_HOTBAR_OFFSET : slot);

        minecraft
                .getToastManager()
                .addToast(new CustomToast(
                        Component.translatable("questaddons.smart_filter.given"),
                        ItemIcon.ofItemStack(stack),
                        Component.translatable("questaddons.smart_filter.given_detail", itemIds.size())));
        return true;
    }

    private static boolean failed(String messageKey) {
        QuestScreen.displayError(Component.translatable(messageKey));
        return true;
    }
}
