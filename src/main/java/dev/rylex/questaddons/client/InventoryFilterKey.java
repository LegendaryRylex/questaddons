package dev.rylex.questaddons.client;

import dev.rylex.questaddons.QuestAddons;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = QuestAddons.MOD_ID, value = Dist.CLIENT)
public final class InventoryFilterKey {
    private InventoryFilterKey() {}

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen)) {
            return;
        }
        if (screen.getFocused() instanceof EditBox editBox && editBox.canConsumeInput()) {
            return;
        }
        if (!QuestAddonsKeys.matches(QuestAddonsKeys.QUEST_FILTER, event.getKeyEvent())) {
            return;
        }

        if (SmartFilterFromInventory.give(event.getKeyEvent().hasShiftDown())) {
            event.setCanceled(true);
        }
    }
}
