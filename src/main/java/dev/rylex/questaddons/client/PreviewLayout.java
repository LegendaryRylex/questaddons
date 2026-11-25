package dev.rylex.questaddons.client;

public final class PreviewLayout {
    public static final int GAP = 6;
    private static final int MIN_PREVIEW = 80;
    private static final int MIN_EDITOR = 120;

    private static boolean shown;

    private PreviewLayout() {}

    public static boolean shown() {
        return shown;
    }

    public static void toggle() {
        shown = !shown;
    }

    public static int previewWidth(int preferred, int area) {
        int max = area - MIN_EDITOR;
        if (max < MIN_PREVIEW) {
            return Math.max(0, area / 2);
        }
        return Math.min(preferred, max);
    }
}
