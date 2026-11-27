package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.TextField;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.client.RewardQuestLink;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.RewardSelectorScreen$RowPanel", remap = false)
public abstract class RewardSelectorRowMixin extends Panel {
    private RewardSelectorRowMixin(Panel panel) {
        super(panel);
    }

    @ModifyExpressionValue(
            method = "addWidgets",
            at =
                    @At(
                            value = "NEW",
                            target =
                                    "(Ldev/ftb/mods/ftblibrary/client/gui/widget/Panel;)Ldev/ftb/mods/ftblibrary/client/gui/widget/TextField;"))
    private TextField questaddons$linkQuestTitle(TextField title) {
        Quest quest = (Quest) ((GroupedRowPanelAccessor) (Object) this).questaddons$getValue();
        return new RewardQuestLink(this, quest, ((RewardSelectorScreenAccessor) getGui()).questaddons$getQuestScreen());
    }
}
