package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.TextField;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.rylex.questaddons.client.InlineImages;
import dev.rylex.questaddons.client.InlineTextField;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TextField.class, remap = false)
public abstract class TextFieldMixin extends Widget implements InlineTextField {
    @Shadow
    public int textFlags;

    @Shadow
    public Color4I textColor;

    @Unique
    private InlineImages.@Nullable Layout questaddons$inline;

    private TextFieldMixin(Panel panel) {
        super(panel);
    }

    @Shadow
    public abstract void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h);

    @Override
    public void questaddons$setInline(InlineImages.@Nullable Layout layout) {
        questaddons$inline = layout;
        if (layout != null) {
            setHeight(layout.height());
        }
    }

    @Inject(
            method =
                    "setText(Lnet/minecraft/network/chat/Component;)Ldev/ftb/mods/ftblibrary/client/gui/widget/TextField;",
            at = @At("HEAD"))
    private void questaddons$clearInline(Component text, CallbackInfoReturnable<TextField> cir) {
        questaddons$inline = null;
    }

    @Inject(method = "resize", at = @At("RETURN"))
    private void questaddons$keepInlineHeight(Theme theme, CallbackInfoReturnable<TextField> cir) {
        if (questaddons$inline != null) {
            setHeight(questaddons$inline.height());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void questaddons$drawInline(
            GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
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
            IconHelper.renderIcon(
                    (dev.ftb.mods.ftblibrary.icon.Icon) image.icon(),
                    graphics,
                    x + image.x(),
                    y + image.y(),
                    image.width(),
                    image.height());
        }
    }

    @Inject(method = "getClickableStyleAt", at = @At("HEAD"), cancellable = true)
    private void questaddons$inlineStyleAt(
            Theme theme, int mouseX, int mouseY, CallbackInfoReturnable<Optional<Style>> cir) {
        if (questaddons$inline != null) {
            cir.setReturnValue(questaddons$inline.styleAt(mouseX - getX(), mouseY - getY()));
        }
    }
}
