package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import java.util.Collection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ViewQuestPanel.class, remap = false)
public interface ViewQuestPanelAccessor {
    @Invoker("showList")
    void questaddons$showList(Collection<QuestObject> objects, boolean dependencies);
}
