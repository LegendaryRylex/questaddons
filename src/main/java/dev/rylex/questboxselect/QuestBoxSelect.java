package dev.rylex.questboxselect;

import dev.rylex.questboxselect.client.BoxSelectKeys;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(QuestBoxSelect.MOD_ID)
public final class QuestBoxSelect {
    public static final String MOD_ID = "questboxselect";

    public QuestBoxSelect(IEventBus modBus, ModContainer container, Dist dist) {
        if (dist.isClient()) {
            BoxSelectKeys.init(modBus);
        }
    }
}
