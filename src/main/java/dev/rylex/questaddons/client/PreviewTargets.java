package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftbquests.quest.Quest;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

public final class PreviewTargets {
    private static final Map<ListConfig<?, ?>, Target> TARGETS = new WeakHashMap<>();

    private PreviewTargets() {}

    public static void register(ListConfig<?, ?> config, Quest quest, int viewWidth) {
        TARGETS.put(config, new Target(quest, viewWidth));
    }

    @Nullable
    public static Target find(ListConfig<?, ?> config) {
        return TARGETS.get(config);
    }

    public record Target(Quest quest, int viewWidth) {}
}
