package dev.rylex.questboxselect;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(QuestBoxSelect.MOD_ID)
public final class QuestBoxSelect {
    public static final String MOD_ID = "questboxselect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public QuestBoxSelect(IEventBus modBus, ModContainer container, Dist dist) {
        LOGGER.info("{} initialized", MOD_ID);
    }
}
