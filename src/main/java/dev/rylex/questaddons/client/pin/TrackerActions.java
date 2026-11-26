package dev.rylex.questaddons.client.pin;

import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.rylex.questaddons.compat.jei.JeiCompat;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class TrackerActions {
    private TrackerActions() {}

    @Nullable
    static TeamData teamData() {
        return ClientQuestFile.exists() ? ClientQuestFile.getInstance().selfTeamData : null;
    }

    static void clickTask(Task task, boolean right) {
        TeamData data = teamData();
        if (data == null) {
            return;
        }
        boolean canClick = task.isValid() && data.canStartTasks(task.getQuest()) && !data.isCompleted(task);
        if (task instanceof ItemTask item) {
            if (!right && canClick && item.consumesResources()) {
                submit(task);
            } else {
                displayStack(item).ifPresent(stack -> showRecipes(stack, right));
            }
            return;
        }
        if (!right && canClick && task.autoSubmitOnPlayerTick() <= 0) {
            submit(task);
        }
    }

    public static Optional<ItemStack> displayStack(Task task) {
        if (task.getIcon().getIngredient() instanceof ItemStack stack && !stack.isEmpty()) {
            return Optional.of(stack);
        }
        if (task instanceof ItemTask item) {
            List<ItemStack> valid = item.getValidDisplayItems();
            if (!valid.isEmpty()) {
                return Optional.of(valid.getFirst());
            }
            return Optional.of(item.getItemStack()).filter(stack -> !stack.isEmpty());
        }
        return Optional.empty();
    }

    public static long amountNeeded(Task task) {
        if (!(task instanceof ItemTask)) {
            return 1L;
        }
        TeamData data = teamData();
        long progress = data == null ? 0L : data.getProgress(task);
        return Math.max(1L, task.getMaxProgress() - progress);
    }

    private static void showRecipes(ItemStack stack, boolean uses) {
        if (JeiCompat.isLoaded()) {
            JeiCompat.showRecipes(stack, uses);
        } else if (!uses) {
            FTBQuests.getRecipeModHelper().showRecipes(stack);
        }
    }

    private static void submit(Task task) {
        playClick();
        Play2ServerNetworking.send(new SubmitTaskMessage(task.id));
    }

    static void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
