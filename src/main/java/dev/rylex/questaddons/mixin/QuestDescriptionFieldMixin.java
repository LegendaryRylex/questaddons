package dev.rylex.questaddons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.WidgetType;
import dev.rylex.questaddons.client.InlineImages;
import dev.rylex.questaddons.client.InlineTextField;
import dev.rylex.questaddons.client.ItemLinkTooltip;
import dev.rylex.questaddons.client.ItemRecipeClick;
import dev.rylex.questaddons.client.RecipeLinkClick;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel$QuestDescriptionField", remap = false)
public abstract class QuestDescriptionFieldMixin extends TextField implements InlineTextField {
    @Unique
    private InlineImages.@Nullable Layout questaddons$inline;

    private QuestDescriptionFieldMixin(Panel panel) {
        super(panel);
    }

    @Override
    public void questaddons$setInline(InlineImages.@Nullable Layout layout) {
        questaddons$inline = layout;
        if (layout != null) {
            setHeight(layout.height());
        }
    }

    @Override
    public TextField setText(Component text) {
        questaddons$inline = null;
        return super.setText(text);
    }

    @Override
    public TextField resize(Theme theme) {
        TextField result = super.resize(theme);
        if (questaddons$inline != null) {
            setHeight(questaddons$inline.height());
        }
        return result;
    }

    @Override
    public Optional<Style> getComponentStyleAt(Theme theme, int mouseX, int mouseY) {
        if (questaddons$inline != null) {
            return questaddons$inline.styleAt(mouseX - getX(), mouseY - getY());
        }
        return super.getComponentStyleAt(theme, mouseX, mouseY);
    }

    @Inject(
            method = "draw",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftblibrary/ui/TextField;draw(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V"),
            cancellable = true)
    private void questaddons$drawInline(
            GuiGraphics graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        InlineImages.Layout layout = questaddons$inline;
        if (layout == null) {
            return;
        }
        ci.cancel();
        drawBackground(graphics, theme, x, y, w, h);
        Color4I color = textColor.isEmpty()
                ? theme.getContentColor(WidgetType.mouseOver(Bits.getFlag(textFlags, Theme.MOUSE_OVER)))
                : textColor;
        int flags = textFlags & ~(Theme.CENTERED | Theme.CENTERED_V);
        for (InlineImages.Line line : layout.lines()) {
            int textY = y + line.y() + line.textY();
            for (InlineImages.Run run : line.runs()) {
                theme.drawString(graphics, run.text(), x + run.x(), textY, color, flags);
            }
        }
        for (InlineImages.Placed image : layout.images()) {
            image.icon().draw(graphics, x + image.x(), y + image.y(), image.width(), image.height());
        }
    }

    @Inject(method = "handleCustomClickEvent", at = @At("HEAD"), cancellable = true)
    private void questaddons$showItemRecipes(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (ItemRecipeClick.show(style)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleCustomClickEvent", at = @At("HEAD"), cancellable = true)
    private void questaddons$openRecipeLink(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (RecipeLinkClick.open(style)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleCustomClickEvent", at = @At("HEAD"), cancellable = true)
    private void questaddons$runImageClick(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (InlineImages.click(style)) {
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
