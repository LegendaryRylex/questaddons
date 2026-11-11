package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.MultilineTextBox;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen;
import dev.rylex.questaddons.client.ComponentJson;
import dev.rylex.questaddons.client.DescriptionEditor;
import dev.rylex.questaddons.client.DescriptionInsert;
import dev.rylex.questaddons.client.DescriptionJsonFixer;
import dev.rylex.questaddons.client.ElementInsertion;
import dev.rylex.questaddons.client.InsertContext;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MultilineTextEditorScreen.class, remap = false)
public abstract class MultilineTextEditorScreenMixin implements DescriptionEditor {
    @Shadow
    @Final
    private MultilineTextBox textBox;

    @Shadow
    @Final
    private Map<Integer, Runnable> hotKeys;

    @Unique
    private InsertContext questaddons$insertContext;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void questaddons$registerFixJsonHotkey(CallbackInfo ci) {
        hotKeys.put(GLFW.GLFW_KEY_F, this::questaddons$fixJson);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void questaddons$closePromptOnEscape(Key key, CallbackInfoReturnable<Boolean> cir) {
        if (key.esc() && questaddons$insertContext != null && questaddons$insertContext.dismissPrompt()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void questaddons$fixJson() {
        String text = textBox.getText();
        DescriptionJsonFixer.Result result = DescriptionJsonFixer.fix(text, FTBQuestsClient.holderLookup());
        int cursor = textBox.cursorPos();
        if (result.fixed() > 0) {
            textBox.setText(result.text());
            cursor = Math.min(cursor, result.text().length());
        }
        if (!result.unfixable().isEmpty()) {
            cursor = lineStart(result.text(), result.unfixable().getFirst());
        }
        textBox.seekCursor(Whence.ABSOLUTE, cursor);
        textBox.setFocused(true);
        toast(result);
    }

    @Override
    public void questaddons$insert(DescriptionInsert insert, int anchorX, int anchorY) {
        MultilineTextBox.StringExtents selected = textBox.getSelected();
        int start = selected.start();
        int end = selected.end();
        String selection = textBox.getText().substring(start, end);
        textBox.setFocused(false);
        questaddons$insertContext = new InsertContext(
                (BaseScreen) (Object) this,
                anchorX,
                anchorY,
                selection.indexOf('\n') >= 0 ? "" : selection,
                element -> questaddons$put(start, end, element),
                () -> textBox.setFocused(true));
        insert.open(questaddons$insertContext);
    }

    private void questaddons$put(int start, int end, String element) {
        String text = textBox.getText();
        int from = Math.min(start, text.length());
        ElementInsertion.Result result = ElementInsertion.insert(
                text,
                from,
                Math.max(from, Math.min(end, text.length())),
                element,
                marked -> ComponentJson.plainLineToJson(marked, element, FTBQuestsClient.holderLookup()));
        textBox.setSelecting(false);
        textBox.setText(result.text());
        textBox.seekCursor(Whence.ABSOLUTE, result.cursor());
        textBox.setFocused(true);
    }

    private static void toast(DescriptionJsonFixer.Result result) {
        Component title = Component.translatable("questaddons.gui.fix_json");
        Component message;
        if (!result.unfixable().isEmpty()) {
            String lines = result.unfixable().stream().map(String::valueOf).collect(Collectors.joining(", "));
            message = Component.translatable("questaddons.gui.fix_json.unfixable", result.fixed(), lines);
        } else if (result.fixed() > 0) {
            message = Component.translatable("questaddons.gui.fix_json.fixed", result.fixed());
        } else {
            message = Component.translatable("questaddons.gui.fix_json.nothing");
        }
        Minecraft.getInstance()
                .getToasts()
                .addToast(new CustomToast(title, result.unfixable().isEmpty() ? Icons.CHECK : Icons.BARRIER, message));
    }

    private static int lineStart(String text, int line) {
        int pos = 0;
        for (int i = 1; i < line; i++) {
            pos = text.indexOf('\n', pos) + 1;
        }
        return pos;
    }
}
