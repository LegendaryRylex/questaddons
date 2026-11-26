package dev.rylex.questaddons.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import dev.rylex.questaddons.QuestAddons;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public final class QuestAddonsKeys {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(QuestAddons.id("keys"));

    public static final KeyMapping BOX_SELECT = unbound("box_select");

    public static final KeyMapping MOVE_SELECTION = unbound("move_selection");

    public static final KeyMapping TOGGLE_OPTIONAL = unbound("toggle_optional");

    public static final KeyMapping QUEST_FILTER = unbound("quest_filter");

    public static final KeyMapping SPLIT_VIEW = unbound("split_view");

    public static final KeyMapping SPLIT_VIEW_STACKED = unbound("split_view_stacked");

    public static final KeyMapping FIX_JSON = unbound("fix_json");

    public static final KeyMapping TOGGLE_PREVIEW = unbound("toggle_preview");

    private QuestAddonsKeys() {}

    private static KeyMapping unbound(String name) {
        return new KeyMapping(
                "key.questaddons.keys." + name,
                KeyConflictContext.GUI,
                KeyModifier.NONE,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY);
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.registerCategory(CATEGORY);
            event.register(BOX_SELECT);
            event.register(MOVE_SELECTION);
            event.register(TOGGLE_OPTIONAL);
            event.register(QUEST_FILTER);
            event.register(SPLIT_VIEW);
            event.register(SPLIT_VIEW_STACKED);
            event.register(FIX_JSON);
            event.register(TOGGLE_PREVIEW);
        });
    }

    public static Optional<Component> hint(KeyMapping mapping) {
        return mapping.isUnbound()
                ? Optional.empty()
                : Optional.of(Component.translatable("questaddons.gui.hotkey", mapping.getTranslatedKeyMessage())
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static boolean isBoxSelectHeld() {
        return isHeld(BOX_SELECT);
    }

    public static boolean isMoveSelectionHeld() {
        return isHeld(MOVE_SELECTION);
    }

    public static boolean isToggleOptionalHeld() {
        return isHeld(TOGGLE_OPTIONAL);
    }

    public static boolean matches(KeyMapping mapping, KeyEvent keyEvent) {
        InputConstants.Key key = mapping.getKey();
        return key.getValue() != InputConstants.UNKNOWN.getValue() && key.equals(InputConstants.getKey(keyEvent));
    }

    private static boolean isHeld(KeyMapping mapping) {
        InputConstants.Key key = mapping.getKey();
        if (key.getValue() == InputConstants.UNKNOWN.getValue()) {
            return false;
        }

        Window window = Minecraft.getInstance().getWindow();
        return switch (key.getType()) {
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case MOUSE -> GLFW.glfwGetMouseButton(window.handle(), key.getValue()) == GLFW.GLFW_PRESS;
            case SCANCODE -> false;
        };
    }
}
