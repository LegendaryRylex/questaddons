package dev.rylex.questboxselect.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class BoxSelectKeys {
    public static final String CATEGORY = "key.categories.questboxselect";

    public static final KeyMapping BOX_SELECT = new KeyMapping(
            "key.questboxselect.box_select",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            CATEGORY);

    public static final KeyMapping MOVE_SELECTION = new KeyMapping(
            "key.questboxselect.move_selection",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            CATEGORY);

    private BoxSelectKeys() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.register(BOX_SELECT);
            event.register(MOVE_SELECTION);
        });
    }

    public static boolean isBoxSelectHeld() {
        return isHeld(BOX_SELECT);
    }

    public static boolean isMoveSelectionHeld() {
        return isHeld(MOVE_SELECTION);
    }

    private static boolean isHeld(KeyMapping mapping) {
        InputConstants.Key key = mapping.getKey();
        if (key.getValue() == InputConstants.UNKNOWN.getValue()) {
            return false;
        }

        long window = Minecraft.getInstance().getWindow().getWindow();
        return switch (key.getType()) {
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            case SCANCODE -> false;
        };
    }
}
