package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableList;
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
                                    "Ldev/ftb/mods/ftblibrary/client/config/EditableConfigGroup;add(Ljava/lang/String;Ldev/ftb/mods/ftblibrary/client/config/editable/EditableConfigValue;Ljava/lang/Object;Ljava/util/function/Consumer;Ljava/lang/Object;)Ldev/ftb/mods/ftblibrary/client/config/editable/EditableConfigValue;"),
            index = 1)
    private EditableConfigValue<?> questaddons$trackDescription(EditableConfigValue<?> value) {
        if (value instanceof EditableList<?, ?> list) {
            PreviewTargets.register(list, (Quest) (Object) this, -1);
        }
        return value;
    }
}
