package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import org.jetbrains.annotations.Nullable;

/** Scoped to one screen so a box drag in a split pane leaves the other side alone. */
public final class BoxSelectState {
    @Nullable
    private static BaseScreen owner;

    private BoxSelectState() {}

    public static void begin(BaseScreen screen) {
        owner = screen;
    }

    public static void end() {
        owner = null;
    }

    public static boolean isActive(BaseScreen screen) {
        return owner == screen;
    }
}
