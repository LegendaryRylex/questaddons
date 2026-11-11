package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.rylex.questaddons.client.ItemLinkTooltip;
import dev.rylex.questaddons.client.ItemRecipeClick;
import java.util.Optional;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel$QuestDescriptionField", remap = false)
public abstract class QuestDescriptionFieldMixin {
    @Inject(method = "handleCustomClickEvent", at = @At("HEAD"), cancellable = true)
    private void questaddons$showItemRecipes(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (ItemRecipeClick.show(style)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(
            method = "addMouseOverText",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftbquests/client/gui/quests/ViewQuestPanel$QuestDescriptionField;getComponentStyleAt(Ldev/ftb/mods/ftblibrary/ui/Theme;II)Ljava/util/Optional;"))
    private Optional<Style> questaddons$markItemTooltip(Optional<Style> style) {
        ItemLinkTooltip.hover(style);
        return style;
    }
}
