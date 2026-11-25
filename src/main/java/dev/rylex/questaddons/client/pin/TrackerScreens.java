package dev.rylex.questaddons.client.pin;

import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.rylex.questaddons.compat.jei.JeiCompat;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

public final class TrackerScreens {
    private TrackerScreens() {}

    public static boolean drawsTracker(@Nullable Screen screen) {
        if (screen == null) {
            return false;
        }
        if (screen instanceof AbstractContainerScreen<?>
                || screen instanceof ScreenWrapper wrapper && wrapper.getGui() instanceof QuestScreen) {
            return true;
        }
        return JeiCompat.isLoaded() && JeiCompat.isJeiScreen(screen);
    }

    public static boolean showsTracker(@Nullable Screen screen) {
        return PinnedTracker.get().hasRows() && drawsTracker(screen);
    }
}
