package dev.rylex.questaddons.client;

public final class GridSnap {
    public static final double STEP = 0.25D;

    /** Reciprocal of {@link #STEP}, matching the {@code snap} local FTB Quests divides by. */
    public static final double SNAP = 1D / STEP;

    private GridSnap() {}
}
