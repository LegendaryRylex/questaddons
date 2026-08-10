package dev.rylex.questboxselect.mixin;

import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Movable;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = QuestScreen.class, remap = false)
public interface QuestScreenAccessor {
    @Accessor("grabbed")
    MouseButton questboxselect$grabbed();

    @Accessor("prevMouseX")
    int questboxselect$prevMouseX();

    @Accessor("prevMouseY")
    int questboxselect$prevMouseY();

    @Accessor("selectedObjects")
    List<Movable> questboxselect$selectedObjects();

    @Accessor("movingObjects")
    void questboxselect$setMovingObjects(boolean movingObjects);

    @Invoker("selectAllQuestsInBox")
    void questboxselect$selectAllQuestsInBox(int mouseX, int mouseY, double scrollX, double scrollY);
}
