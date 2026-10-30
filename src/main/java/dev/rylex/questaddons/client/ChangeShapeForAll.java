package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenu;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.rylex.questaddons.mixin.QuestAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class ChangeShapeForAll {
    private static final String DEFAULT_SHAPE = "default";
    private static final String ANCHOR_KEY = "ftbquests.gui.bulk_change_size";

    private ChangeShapeForAll() {}

    public static void insertInto(List<ContextMenuItem> menu, Quest clicked, List<Quest> selected) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.get(i).getTitle().getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals(ANCHOR_KEY)) {
                String shape = ((QuestAccessor) (Object) clicked).questaddons$getShape();
                menu.add(i + 1, subMenu(selected, shape.isEmpty() ? DEFAULT_SHAPE : shape));
                return;
            }
        }
    }

    private static ContextMenuItem subMenu(List<Quest> quests, String current) {
        List<ContextMenuItem> options = new ArrayList<>();
        for (String shape : QuestShape.idMapWithDefault) {
            options.add(new ContextMenuItem(
                    QuestShape.idMapWithDefault.getDisplayName(shape),
                    shape.equals(current) ? Icons.CHECK : Icon.empty(),
                    button -> apply(quests, shape)));
        }
        Component title = Component.translatable(
                "questaddons.gui.change_shape_all", QuestShape.idMapWithDefault.getDisplayName(current));
        return new ContextMenuItem(title, Icons.SETTINGS, button -> openOptions(button, options)).setCloseMenu(false);
    }

    private static void openOptions(Button button, List<ContextMenuItem> options) {
        ContextMenu menu = new ContextMenu(button.getParent(), options);
        BaseScreen gui = button.getGui();
        gui.openContextMenu(menu);
        int x = MenuPlacement.beside(
                button.getX(), button.getX() + button.width, menu.width, gui.getX(), gui.getX() + gui.width);
        int y = MenuPlacement.within(button.getY(), menu.height, gui.getY(), gui.getY() + gui.height);
        menu.setPos(x - button.getX() + button.getPosX(), y - button.getY() + button.getPosY());
    }

    private static void apply(List<Quest> quests, String shape) {
        String stored = shape.equals(DEFAULT_SHAPE) ? "" : shape;
        quests.forEach(quest -> ((QuestAccessor) (Object) quest).questaddons$setShape(stored));
        Play2ServerNetworking.send(EditObjectMessage.forQuestObjects(quests));
    }
}
