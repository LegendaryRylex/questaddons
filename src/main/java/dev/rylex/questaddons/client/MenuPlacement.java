package dev.rylex.questaddons.client;

public final class MenuPlacement {
    private MenuPlacement() {}

    public static int beside(int anchorStart, int anchorEnd, int size, int limitStart, int limitEnd) {
        int placed = anchorEnd + size > limitEnd ? anchorStart - size : anchorEnd;
        return within(placed, size, limitStart, limitEnd);
    }

    public static int within(int position, int size, int limitStart, int limitEnd) {
        return Math.max(limitStart, Math.min(position, limitEnd - size));
    }
}
