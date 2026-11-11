package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.client.PreviewTargets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Quest.class, remap = false)
public abstract class QuestDescriptionConfigMixin {
    @ModifyArg(
            method = "fillConfigGroup",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftblibrary/config/ConfigGroup;add(Ljava/lang/String;Ldev/ftb/mods/ftblibrary/config/ConfigValue;Ljava/lang/Object;Ljava/util/function/Consumer;Ljava/lang/Object;)Ldev/ftb/mods/ftblibrary/config/ConfigValue;"),
            index = 1)
    private ConfigValue<?> questaddons$trackDescription(ConfigValue<?> value) {
        if (value instanceof ListConfig<?, ?> list) {
            PreviewTargets.register(list, (Quest) (Object) this, -1);
        }
        return value;
    }
}
