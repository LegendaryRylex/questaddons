package dev.rylex.questaddons.client;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ContextMenu;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestShape;
import dev.rylex.questaddons.mixin.QuestAccessor;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class ChangeShapeForAll extends ContextMenuItem {
    private static final String DEFAULT_SHAPE = "default";
    private static final String ANCHOR_KEY = "ftbquests.gui.bulk_change_size";

    private final List<Quest> quests;
    private String shape;

    private ChangeShapeForAll(List<Quest> quests, String shape) {
        super(label(shape), Icons.SETTINGS, button -> {});
        this.quests = quests;
        this.shape = shape;
        setCloseMenu(false);
    }

    public static void insertInto(List<ContextMenuItem> menu, Quest clicked, List<Quest> selected) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.get(i).getTitle().getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals(ANCHOR_KEY)) {
                String shape = ((QuestAccessor) (Object) clicked).questaddons$getShape();
                menu.add(i + 1, new ChangeShapeForAll(selected, shape.isEmpty() ? DEFAULT_SHAPE : shape));
                return;
            }
        }
    }

    private static Component label(String shape) {
        return Component.translatable(
                "questaddons.gui.change_shape_all", QuestShape.idMapWithDefault.getDisplayName(shape));
    }

    @Override
    public Widget createWidget(ContextMenu panel) {
        Widget widget = super.createWidget(panel);
        Theme theme = panel.getGui().getTheme();
        int widest = widget.width;
        for (String candidate : QuestShape.idMapWithDefault) {
            widest = Math.max(widest, theme.getStringWidth(label(candidate)) + (panel.hasIcons() ? 14 : 4));
        }
        widget.setWidth(widest);
        return widget;
    }

    @Override
    public void onClicked(Button button, Panel panel, MouseButton mouseButton) {
        NameMap<String> shapes = QuestShape.idMapWithDefault;
        shape = mouseButton.isRight() ? shapes.getPrevious(shape) : shapes.getNext(shape);
        String stored = shape.equals(DEFAULT_SHAPE) ? "" : shape;
        quests.forEach(quest -> ((QuestAccessor) (Object) quest).questaddons$setShape(stored));
        NetworkManager.sendToServer(EditObjectMessage.forQuestObjects(quests));
        button.setTitle(label(shape));
    }
}
