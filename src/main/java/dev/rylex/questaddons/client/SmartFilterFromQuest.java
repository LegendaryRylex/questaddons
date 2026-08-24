package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.rylex.questaddons.compat.ftbfiltersystem.FilterSystemCompat;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class SmartFilterFromQuest {
    private static final int MENU_HOTBAR_OFFSET = 36;

    private SmartFilterFromQuest() {}

    public static boolean give(Quest quest) {
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

        Set<Identifier> itemIds = taskItemIds(quest);
        Optional<ItemStack> filter = FilterSystemCompat.orFilterOf(itemIds);
        if (filter.isEmpty()) {
            return failed("questaddons.smart_filter.no_items");
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

    private static Set<Identifier> taskItemIds(Quest quest) {
        Set<Identifier> itemIds = new LinkedHashSet<>();
        for (Task task : quest.getTasks()) {
            if (task instanceof ItemTask itemTask) {
                for (ItemStack stack : itemTask.getValidDisplayItems()) {
                    if (!stack.isEmpty()) {
                        itemIds.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
                    }
                }
            }
        }
        return itemIds;
    }

    private static boolean failed(String messageKey) {
        QuestScreen.displayError(Component.translatable(messageKey));
        return true;
    }
}
