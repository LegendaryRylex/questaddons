package dev.rylex.questaddons.client;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.rylex.questaddons.compat.ftbfiltersystem.FilterSyntax;
import dev.rylex.questaddons.compat.ftbfiltersystem.FilterSystemCompat;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class SmartFilterFromQuest {
    private SmartFilterFromQuest() {}

    public static boolean give(Quest quest) {
        return SmartFilter.give(taskTerms(quest), "questaddons.smart_filter.no_items");
    }

    private static Set<String> taskTerms(Quest quest) {
        Set<String> terms = new LinkedHashSet<>();
        for (Task task : quest.getTasks()) {
            if (task instanceof ItemTask itemTask) {
                addTerms(terms, itemTask);
            }
        }
        return terms;
    }

    private static void addTerms(Set<String> terms, ItemTask task) {
        Optional<String> taskFilter = FilterSystemCompat.filterOf(task.getItemStack());
        if (taskFilter.isPresent()) {
            terms.add(taskFilter.get());
            return;
        }
        for (ItemStack stack : task.getValidDisplayItems()) {
            if (!stack.isEmpty() && !FilterSystemCompat.isSmartFilter(stack)) {
                terms.add(FilterSyntax.item(
                        BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()));
            }
        }
    }
}
