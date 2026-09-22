package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Movable;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = QuestScreen.class, remap = false)
public interface QuestScreenAccessor {
    @Accessor("selectedChapter")
    Chapter questaddons$selectedChapter();

    @Accessor("selectedObjects")
    List<Movable> questaddons$selectedObjects();

    @Accessor("movingObjects")
    void questaddons$setMovingObjects(boolean movingObjects);
}
