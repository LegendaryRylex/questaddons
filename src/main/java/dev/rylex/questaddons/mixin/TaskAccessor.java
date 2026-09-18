package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.quest.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Task.class, remap = false)
public interface TaskAccessor {
    @Accessor("optionalTask")
    boolean questaddons$isOptionalTask();

    @Accessor("optionalTask")
    void questaddons$setOptionalTask(boolean optionalTask);
}
