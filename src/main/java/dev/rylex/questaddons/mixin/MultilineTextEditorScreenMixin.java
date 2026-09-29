package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.widget.MultilineTextBox;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.CustomToast;
import dev.ftb.mods.ftbquests.client.gui.MultilineTextEditorScreen;
import dev.rylex.questaddons.client.DescriptionJsonFixer;
import dev.rylex.questaddons.client.JsonFixHost;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultilineTextEditorScreen.class, remap = false)
public abstract class MultilineTextEditorScreenMixin implements JsonFixHost {
    @Shadow
    @Final
    private MultilineTextBox textBox;

    @Shadow
    @Final
    private Map<Integer, Runnable> hotKeys;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void questaddons$registerFixJsonHotkey(CallbackInfo ci) {
        hotKeys.put(GLFW.GLFW_KEY_F, this::questaddons$fixJson);
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
                .getToastManager()
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
