package dev.rylex.questaddons.client;

import net.minecraft.util.Mth;

public final class PreviewLayout {
    public static final int DIVIDER = 6;
    private static final int MIN_PREVIEW = 80;
    private static final int MIN_EDITOR = 120;

    private static boolean shown;
    private static double fraction = 0.5;

    private PreviewLayout() {}

    public static boolean shown() {
        return shown;
    }

    public static void toggle() {
        shown = !shown;
    }

    public static int previewWidth(int area) {
        return clampWidth((int) Math.round(area * fraction), area);
    }

    public static void dragTo(int offset, int area) {
        if (area > 0) {
            fraction = (double) clampWidth(offset, area) / area;
        }
    }

    private static int clampWidth(int width, int area) {
        int max = area - MIN_EDITOR;
        if (max < MIN_PREVIEW) {
            return Math.max(0, area / 2);
        }
        return Mth.clamp(width, MIN_PREVIEW, max);
    }
}
