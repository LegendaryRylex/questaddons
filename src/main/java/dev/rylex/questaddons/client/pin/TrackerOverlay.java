package dev.rylex.questaddons.client.pin;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class TrackerOverlay {
    private static final int SLOT_HIGHLIGHT = 0x8060C0FF;

    private static final BitSet CONTAINER_SLOTS = new BitSet();
    private static final List<ItemTask> WANTED = new ArrayList<>();

    private TrackerOverlay() {}

    static void renderOverScreen(GuiGraphicsExtractor graphics, Screen screen, int mouseX, int mouseY) {
        if (TrackerActions.teamData() == null || !TrackerScreens.showsTracker(screen)) {
            return;
        }
        if (screen instanceof AbstractContainerScreen<?> container) {
            drawSlotHighlights(graphics, container);
        }
        graphics.nextStratum();
        PinnedTracker tracker = PinnedTracker.get();
        tracker.renderOverScreen(graphics, mouseX, mouseY);
        tracker.drawTooltip(graphics, mouseX, mouseY);
    }

    static void tick(Minecraft minecraft) {
        CONTAINER_SLOTS.clear();
        TeamData data = TrackerActions.teamData();
        if (data == null || !(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        WANTED.clear();
        for (Quest quest : PinnedTracker.get().quests()) {
            if (!data.canStartTasks(quest)) {
                continue;
            }
            for (Task task : quest.getTasksAsList()) {
                if (task instanceof ItemTask item && !data.isCompleted(item)) {
                    WANTED.add(item);
                }
            }
        }
        if (WANTED.isEmpty()) {
            return;
        }
        List<Slot> slots = screen.getMenu().slots;
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (!stack.isEmpty() && isWanted(stack)) {
                CONTAINER_SLOTS.set(i);
            }
        }
    }

    private static boolean isWanted(ItemStack stack) {
        for (ItemTask task : WANTED) {
            if (task.test(stack)) {
                return true;
            }
        }
        return false;
    }

    static List<Component> taskTooltip(Task task, TeamData data) {
        TooltipList list = new TooltipList();
        task.addMouseOverHeader(list, data, Minecraft.getInstance().options.advancedItemTooltips);
        if (data.canStartTasks(task.getQuest())) {
            long max = task.getMaxProgress();
            long progress = data.getProgress(task);
            if (max > 1L) {
                if (task.hideProgressNumbers()) {
                    list.add(Component.literal("[" + task.getRelativeProgressFromChildren(data) + "%]")
                            .withStyle(ChatFormatting.DARK_GREEN));
                } else {
                    String maxText = task.formatMaxProgress();
                    String progressText = progress > max ? maxText : task.formatProgress(data, progress);
                    list.add(Component.literal(progressText + " / " + maxText).withStyle(ChatFormatting.DARK_GREEN));
                }
            }
        } else {
            list.add(Component.translatable("questaddons.gui.pinned_tracker.locked")
                    .withStyle(ChatFormatting.RED));
        }
        if (task.isOptionalForProgression(data)) {
            list.add(
                    Component.translatable("ftbquests.quest.misc.optional_task").withStyle(ChatFormatting.GRAY));
        }
        task.addMouseOverText(list, data);
        return list.getLines();
    }

    private static void drawSlotHighlights(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen) {
        List<Slot> slots = screen.getMenu().slots;
        for (int i = CONTAINER_SLOTS.nextSetBit(0); i >= 0 && i < slots.size(); i = CONTAINER_SLOTS.nextSetBit(i + 1)) {
            Slot slot = slots.get(i);
            if (!slot.isActive()) {
                continue;
            }
            int x = screen.getLeftPos() + slot.x;
            int y = screen.getTopPos() + slot.y;
            graphics.fill(x, y, x + 16, y + 16, SLOT_HIGHLIGHT);
        }
    }
}
