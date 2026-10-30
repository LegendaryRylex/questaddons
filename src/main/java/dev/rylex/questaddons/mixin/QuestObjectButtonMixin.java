package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.rylex.questaddons.client.QuestObjectHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen$QuestObjectButton", remap = false)
public abstract class QuestObjectButtonMixin implements QuestObjectHolder {
    @Shadow
    @Final
    public QuestObjectBase object;

    @Override
    public QuestObjectBase questaddons$object() {
        return object;
    }
}
