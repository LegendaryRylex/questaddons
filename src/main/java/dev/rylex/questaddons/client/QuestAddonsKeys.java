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

    public static final KeyMapping BOX_SELECT = new KeyMapping(
            "key.questaddons.box_select",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            CATEGORY);

    public static final KeyMapping MOVE_SELECTION = new KeyMapping(
            "key.questaddons.move_selection",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            CATEGORY);

    public static final KeyMapping INSTANT_COMPLETE = new KeyMapping(
            "key.questaddons.instant_complete",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY);

    public static final KeyMapping RESET_PROGRESS = new KeyMapping(
            "key.questaddons.reset_progress",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY);

    public static final KeyMapping DELETE_OBJECT = new KeyMapping(
            "key.questaddons.delete_object",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            CATEGORY);

    public static final KeyMapping QUEST_FILTER = new KeyMapping(
            "key.questaddons.quest_filter",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            CATEGORY);

    public static final KeyMapping SAVE_QUESTS = new KeyMapping(
            "key.questaddons.save_quests",
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_S,
            CATEGORY);

    private QuestAddonsKeys() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.register(BOX_SELECT);
            event.register(MOVE_SELECTION);
            event.register(INSTANT_COMPLETE);
            event.register(RESET_PROGRESS);
            event.register(DELETE_OBJECT);
            event.register(QUEST_FILTER);
            event.register(SAVE_QUESTS);
        });
    }

    public static boolean isBoxSelectHeld() {
        return isHeld(BOX_SELECT);
    }

    public static boolean isMoveSelectionHeld() {
        return isHeld(MOVE_SELECTION);
    }

    public static boolean isInstantCompleteHeld() {
        return isHeld(INSTANT_COMPLETE);
    }

    public static boolean isResetProgressHeld() {
        return isHeld(RESET_PROGRESS);
    }

    public static boolean isDeleteObjectHeld() {
        return isHeld(DELETE_OBJECT);
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
