package dev.rylex.questaddons.client;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class SmartFilterFromQuest {
    private SmartFilterFromQuest() {}

    public static boolean give(Quest quest) {
        return SmartFilter.give(taskItemIds(quest), "questaddons.smart_filter.no_items");
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
}
