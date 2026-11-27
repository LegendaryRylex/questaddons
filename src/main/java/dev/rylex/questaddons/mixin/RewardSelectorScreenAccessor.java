package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.gui.RewardSelectorScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RewardSelectorScreen.class, remap = false)
public interface RewardSelectorScreenAccessor {
    @Accessor("questScreen")
    QuestScreen questaddons$getQuestScreen();
}
