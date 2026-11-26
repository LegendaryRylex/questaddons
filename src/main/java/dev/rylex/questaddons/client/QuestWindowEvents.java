package dev.rylex.questaddons.client;

import dev.rylex.questaddons.QuestAddons;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = QuestAddons.MOD_ID, value = Dist.CLIENT)
public final class QuestWindowEvents {
    private QuestWindowEvents() {}

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        QuestWindows.endDrag();
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            QuestWindows.endDrag();
        }
    }
}
