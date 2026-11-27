package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.FTBQuestsKeyMappings;
import dev.ftb.mods.ftbquests.client.gui.quests.TabButton;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SearchTabButton extends TabButton {
    private static final Icon ICON =
            Icon.getIcon(ResourceLocation.fromNamespaceAndPath("ftbquests", "textures/gui/search.png"));

    public SearchTabButton(Panel panel) {
        super(panel, Component.translatable("questaddons.gui.search"), ICON);
    }

    @Override
    public void onClicked(MouseButton button) {
        playClickSound();
        QuestSearchBar.toggle();
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if (QuestSearchBar.isExpanded() && !isMouseOver()) {
            ThemeProperties.WIDGET_BACKGROUND.get().draw(graphics, x + 1, y, w - 2, h);
        }
        super.draw(graphics, theme, x, y, w, h);
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        list.add(Component.translatable(
                QuestSearchBar.isExpanded() ? "questaddons.gui.search.collapse" : "questaddons.gui.search.expand"));
        QuestAddonsKeys.hint(FTBQuestsKeyMappings.KEY_GUI_SEARCH).ifPresent(list::add);
    }
}
