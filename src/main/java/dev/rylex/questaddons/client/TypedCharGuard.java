package dev.rylex.questaddons.client;

import dev.rylex.questaddons.QuestAddons;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = QuestAddons.MOD_ID, value = Dist.CLIENT)
public final class TypedCharGuard {
    private static boolean armed;

    private TypedCharGuard() {}

    public static void swallowNextChar() {
        armed = true;
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        armed = false;
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Pre event) {
        armed = false;
    }

    @SubscribeEvent
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (armed) {
            armed = false;
            event.setCanceled(true);
        }
    }
}
