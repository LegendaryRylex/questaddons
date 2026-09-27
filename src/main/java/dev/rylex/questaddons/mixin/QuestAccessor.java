package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.quest.Quest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Quest.class, remap = false)
public interface QuestAccessor {
    @Accessor("optional")
    void questaddons$setOptional(boolean optional);
}
