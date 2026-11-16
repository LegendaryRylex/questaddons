package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.TextBox;
import net.minecraft.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TextBox.class, remap = false)
public abstract class TextBoxRetypeSelectionMixin {
    @Shadow
    private String text;

    @Shadow
    private int cursorPos;

    @Shadow
    private int highlightPos;

    @Shadow
    public abstract void setCursorPosition(int pos);

    @Shadow
    public abstract void setSelectionPos(int pos);

    @Inject(method = "insertText", at = @At("HEAD"), cancellable = true)
    private void questaddons$collapseSelectionOnRetype(String string, CallbackInfo ci) {
        int selStart = Math.min(cursorPos, highlightPos);
        int selEnd = Math.max(cursorPos, highlightPos);
        if (selStart == selEnd) {
            return;
        }
        if (text.substring(selStart, selEnd).equals(StringUtil.filterText(string))) {
            setCursorPosition(selEnd);
            setSelectionPos(selEnd);
            ci.cancel();
        }
    }
}
