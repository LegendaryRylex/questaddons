package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Movable;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = QuestScreen.class, remap = false)
public interface QuestScreenAccessor {
    @Accessor("grabbed")
    MouseButton questaddons$grabbed();

    @Accessor("prevMouseX")
    int questaddons$prevMouseX();

    @Accessor("prevMouseY")
    int questaddons$prevMouseY();

    @Accessor("selectedObjects")
    List<Movable> questaddons$selectedObjects();

    @Accessor("movingObjects")
    boolean questaddons$movingObjects();

    @Accessor("movingObjects")
    void questaddons$setMovingObjects(boolean movingObjects);

    @Invoker("selectAllQuestsInBox")
    void questaddons$selectAllQuestsInBox(int mouseX, int mouseY, double scrollX, double scrollY);
}
