package dev.rylex.questaddons.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public final class QuestAddonsKeys {
    public static final String CATEGORY = "key.categories.questaddons";

    public static final KeyMapping MOVE_SELECTION = new KeyMapping(
            "key.questaddons.move_selection",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            CATEGORY);

    public static final KeyMapping TOGGLE_OPTIONAL = new KeyMapping(
            "key.questaddons.toggle_optional",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY);

    public static final KeyMapping QUEST_FILTER = new KeyMapping(
            "key.questaddons.quest_filter",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            CATEGORY);

    public static final KeyMapping SPLIT_VIEW = new KeyMapping(
            "key.questaddons.split_view",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY);

    public static final KeyMapping SPLIT_VIEW_STACKED = new KeyMapping(
            "key.questaddons.split_view_stacked",
            KeyConflictContext.GUI,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY);

    private QuestAddonsKeys() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.register(MOVE_SELECTION);
            event.register(TOGGLE_OPTIONAL);
            event.register(QUEST_FILTER);
            event.register(SPLIT_VIEW);
            event.register(SPLIT_VIEW_STACKED);
        });
    }

    public static boolean isMoveSelectionHeld() {
        return isHeld(MOVE_SELECTION);
    }

    public static boolean isToggleOptionalHeld() {
        return isHeld(TOGGLE_OPTIONAL);
    }

    public static boolean matches(KeyMapping mapping, int keyCode, int scanCode) {
        InputConstants.Key key = mapping.getKey();
        return key.getValue() != InputConstants.UNKNOWN.getValue()
                && key.equals(InputConstants.getKey(keyCode, scanCode));
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
