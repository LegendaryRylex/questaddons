package dev.rylex.questaddons.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

/**
 * Remembers the key a click gesture acted on so that its release can be swallowed, because FTB
 * Quests reads several unmodified keys as editor shortcuts on release rather than on press.
 */
public final class ClickGestureGuard {
    private static int armedKey = InputConstants.UNKNOWN.getValue();

    private ClickGestureGuard() {}

    public static void arm(KeyMapping mapping) {
        InputConstants.Key key = mapping.getKey();
        armedKey = key.getType() == InputConstants.Type.KEYSYM ? key.getValue() : InputConstants.UNKNOWN.getValue();
    }

    public static void disarm() {
        armedKey = InputConstants.UNKNOWN.getValue();
    }

    public static boolean isArmed() {
        return armedKey != InputConstants.UNKNOWN.getValue();
    }

    public static int armedKey() {
        return armedKey;
    }
}
