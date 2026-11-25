package dev.rylex.questaddons.compat.neiaddon;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

final class NeiItemKeys {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean unusable;

    private NeiItemKeys() {}

    static boolean keyPressed(ItemStack stack, long amount, int keyCode, boolean allowControl) {
        if (unusable) {
            return false;
        }
        try {
            return handle(stack, amount, keyCode, allowControl);
        } catch (LinkageError e) {
            unusable = true;
            LOGGER.warn("Could not use NEI Features for JEI's item keys; they stay off for quest items", e);
            return false;
        }
    }

    private static boolean handle(ItemStack stack, long amount, int keyCode, boolean allowControl) {
        if (QuantityField.focused()) {
            return false;
        }
        boolean shift = Screen.hasShiftDown();
        boolean control = Screen.hasControlDown();
        if (control && !allowControl) {
            return false;
        }
        if (control && !shift) {
            String copied = copiedText(stack, keyCode);
            if (copied != null) {
                Minecraft.getInstance().keyboardHandler.setClipboard(copied);
                return true;
            }
        }
        if (control && matches(CalcKeys.CHAT_LINK, keyCode)) {
            return ShareClient.send(Bookmarks.shareItem(stack, amount));
        }
        if (shift && matches(CalcKeys.CRAFT, keyCode)) {
            return CraftablesPanel.autocraft(stack, control) || RecipeHearts.autocraftFavorite(stack, control);
        }
        if (matches(CalcKeys.PULL, keyCode)) {
            return Pull.pullItem(stack, amount, shift);
        }
        if (!shift && matches(CalcKeys.ADD, keyCode)) {
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
                stack.getTags().map(tag -> tag.location().toString()).sorted().collect(Collectors.joining(","));
            default -> null;
        };
    }

    private static boolean matches(KeyMapping mapping, int keyCode) {
        return mapping.matches(keyCode, InputConstants.UNKNOWN.getValue());
    }
}
