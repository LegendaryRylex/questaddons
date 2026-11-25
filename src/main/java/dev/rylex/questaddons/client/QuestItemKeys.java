package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.IFocusableWidget;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.QuestAddons;
import dev.rylex.questaddons.client.pin.PinnedTracker;
import dev.rylex.questaddons.client.pin.TrackerActions;
import dev.rylex.questaddons.client.pin.TrackerScreens;
import dev.rylex.questaddons.compat.jei.JeiCompat;
import dev.rylex.questaddons.compat.neiaddon.NeiAddonCompat;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = QuestAddons.MOD_ID, value = Dist.CLIENT)
public final class QuestItemKeys {
    private QuestItemKeys() {}

    private record HoveredItem(ItemStack stack, long amount) {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!NeiAddonCompat.isLoaded()) {
            return;
        }
        Screen screen = event.getScreen();
        if (typing(screen)) {
            return;
        }
        Optional<HoveredItem> hovered = hovered(screen);
        if (hovered.isPresent()
                && NeiAddonCompat.keyPressed(
                        hovered.get().stack(), hovered.get().amount(), event.getKeyCode(), !editingQuests(screen))) {
            event.setCanceled(true);
        }
    }

    private static Optional<HoveredItem> hovered(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        double mouseX = minecraft.mouseHandler.xpos()
                * minecraft.getWindow().getGuiScaledWidth()
                / minecraft.getWindow().getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos()
                * minecraft.getWindow().getGuiScaledHeight()
                / minecraft.getWindow().getScreenHeight();
        PinnedTracker tracker = PinnedTracker.get();
        if (TrackerScreens.showsTracker(screen) && tracker.contains(mouseX, mouseY)) {
            return tracker.taskAt(mouseX, mouseY).flatMap(task -> TrackerActions.displayStack(task)
                    .map(stack -> new HoveredItem(stack, TrackerActions.amountNeeded(task))));
        }
        if (screen instanceof IScreenWrapper wrapper) {
            return ingredientUnderMouse(wrapper.getGui())
                    .map(PositionedIngredient::ingredient)
                    .filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .filter(stack -> !stack.isEmpty())
                    .map(stack -> new HoveredItem(stack, Math.max(1, stack.getCount())));
        }
        return Optional.empty();
    }

    private static Optional<PositionedIngredient> ingredientUnderMouse(BaseScreen gui) {
        if (gui instanceof QuestScreen quests
                && quests.getViewedQuest() != null
                && quests.viewQuestPanel.isMouseOver()) {
            return quests.viewQuestPanel.getIngredientUnderMouse();
        }
        return gui.getIngredientUnderMouse();
    }

    private static boolean editingQuests(Screen screen) {
        return screen instanceof IScreenWrapper wrapper
                && wrapper.getGui() instanceof QuestScreen
                && ClientQuestFile.exists()
                && ClientQuestFile.INSTANCE.canEdit();
    }

    private static boolean typing(Screen screen) {
        if (screen.getFocused() instanceof EditBox box && box.canConsumeInput()) {
            return true;
        }
        if (screen instanceof IScreenWrapper wrapper && hasFocusedField(wrapper.getGui())) {
            return true;
        }
        return JeiCompat.isLoaded() && JeiCompat.hasKeyboardFocus();
    }

    private static boolean hasFocusedField(Panel panel) {
        for (Widget widget : panel.getWidgets()) {
            if (widget instanceof IFocusableWidget field && field.isFocused()) {
                return true;
            }
            if (widget instanceof Panel child && hasFocusedField(child)) {
                return true;
            }
        }
        return false;
    }
}
