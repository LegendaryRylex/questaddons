package dev.rylex.questaddons.compat.neiaddon;

import com.mojang.logging.LogUtils;
import java.util.stream.Collectors;
import neiaddon.Bookmarks;
import neiaddon.CalcKeys;
import neiaddon.CraftablesPanel;
import neiaddon.Pull;
import neiaddon.QuantityField;
import neiaddon.RecipeHearts;
import neiaddon.ShareClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

final class NeiItemKeys {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean unusable;

    private NeiItemKeys() {}

    static boolean keyPressed(ItemStack stack, long amount, KeyEvent keyEvent, boolean allowControl) {
        if (unusable) {
            return false;
        }
        try {
            return handle(stack, amount, keyEvent, allowControl);
        } catch (LinkageError e) {
            unusable = true;
            LOGGER.warn("Could not use NEI Features for JEI's item keys; they stay off for quest items", e);
            return false;
        }
    }

    private static boolean handle(ItemStack stack, long amount, KeyEvent keyEvent, boolean allowControl) {
        if (QuantityField.focused()) {
            return false;
        }
        boolean shift = keyEvent.hasShiftDown();
        boolean control = keyEvent.hasControlDown();
        if (control && !allowControl) {
            return false;
        }
        if (control && !shift) {
            String copied = copiedText(stack, keyEvent.key());
            if (copied != null) {
                Minecraft.getInstance().keyboardHandler.setClipboard(copied);
                return true;
            }
        }
        if (control && matches(CalcKeys.CHAT_LINK, keyEvent)) {
            return ShareClient.send(Bookmarks.shareItem(stack, amount));
        }
        if (shift && matches(CalcKeys.CRAFT, keyEvent)) {
            return CraftablesPanel.autocraft(stack, control) || RecipeHearts.autocraftFavorite(stack, control);
        }
        if (matches(CalcKeys.PULL, keyEvent)) {
            return Pull.pullItem(stack, amount, shift);
        }
        if (!shift && matches(CalcKeys.ADD, keyEvent)) {
            Bookmarks.toggle(stack, control ? amount : 1L);
            return true;
        }
        return false;
    }

    @Nullable
    private static String copiedText(ItemStack stack, int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_C -> stack.getHoverName().getString();
            case GLFW.GLFW_KEY_X ->
                BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            case GLFW.GLFW_KEY_D ->
                stack.typeHolder()
                        .tags()
                        .map(tag -> tag.location().toString())
                        .sorted()
                        .collect(Collectors.joining(","));
            default -> null;
        };
    }

    private static boolean matches(KeyMapping mapping, KeyEvent keyEvent) {
        return mapping.matches(keyEvent);
    }
}
