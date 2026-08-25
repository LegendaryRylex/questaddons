package dev.rylex.questaddons;

import dev.rylex.questaddons.client.QuestAddonsKeys;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(QuestAddons.MOD_ID)
public final class QuestAddons {
    public static final String MOD_ID = "questaddons";

    public QuestAddons(IEventBus modBus, ModContainer container, Dist dist) {
        if (dist.isClient()) {
            QuestAddonsKeys.init(modBus);
        }
    }
}
