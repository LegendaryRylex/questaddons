package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.FTBQuestsKeyMappings;
import dev.ftb.mods.ftbquests.client.gui.quests.TabButton;
import dev.rylex.questaddons.mixin.QuestScreenAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SearchTabButton extends TabButton {
    private static final Icon<?> ICON =
            Icon.getIcon(Identifier.fromNamespaceAndPath("ftbquests", "textures/gui/search.png"));

    public SearchTabButton(Panel panel) {
        super(panel, Component.translatable("questaddons.gui.search"), ICON);
    }

    @Override
    public void onClicked(MouseButton button) {
        playClickSound();
        ((QuestScreenAccessor) questScreen).questaddons$openQuestSelectionGUI();
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        super.addMouseOverText(list);
        QuestAddonsKeys.hint(FTBQuestsKeyMappings.KEY_GUI_SEARCH).ifPresent(list::add);
    }
}
