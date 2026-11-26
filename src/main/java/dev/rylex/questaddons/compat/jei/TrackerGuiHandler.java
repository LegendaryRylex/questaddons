package dev.rylex.questaddons.compat.jei;

import dev.rylex.questaddons.client.pin.PinnedTracker;
import dev.rylex.questaddons.client.pin.TrackerActions;
import dev.rylex.questaddons.client.pin.TrackerScreens;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

final class TrackerGuiHandler implements IGlobalGuiHandler {
    private List<Rect2i> lastAreas = List.of();

    @Override
    public Collection<Rect2i> getGuiExtraAreas() {
        PinnedTracker tracker = PinnedTracker.get();
        if (tracker.isDragging() && Minecraft.getInstance().mouseHandler.isLeftPressed()) {
            return lastAreas;
        }
        lastAreas = TrackerScreens.showsTracker(Minecraft.getInstance().screen) ? List.of(tracker.area()) : List.of();
        return lastAreas;
    }

    @Override
    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
            IClickableIngredientFactory factory, double mouseX, double mouseY) {
        PinnedTracker tracker = PinnedTracker.get();
        if (!TrackerScreens.showsTracker(Minecraft.getInstance().screen) || !tracker.contains(mouseX, mouseY)) {
            return Optional.empty();
        }
        Optional<Rect2i> icon = tracker.iconArea(mouseX, mouseY);
        return tracker.taskAt(mouseX, mouseY)
                .flatMap(TrackerActions::displayStack)
                .flatMap(stack ->
                        icon.flatMap(area -> factory.createBuilder(stack).buildWithArea(area)));
    }
}
