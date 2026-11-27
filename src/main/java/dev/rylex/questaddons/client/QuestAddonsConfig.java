package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientConfig;

public final class QuestAddonsConfig {
    private static final SNBTConfig GROUP = FTBQuestsClientConfig.CONFIG.addGroup("questaddons", 5);
    public static final BooleanValue LIVE_PREVIEW_OPEN = GROUP.addBoolean("live_preview_open", true);
    public static final BooleanValue SEARCH_BAR_EXPANDED = GROUP.addBoolean("search_bar_expanded", true);

    private QuestAddonsConfig() {}

    public static void init() {}
}
