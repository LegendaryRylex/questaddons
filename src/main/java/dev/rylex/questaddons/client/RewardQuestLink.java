package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.ui.CursorType;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.TextField;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.jetbrains.annotations.Nullable;

public final class RewardQuestLink extends TextField {
    private final Quest quest;

    @Nullable
    private final QuestScreen questScreen;

    public RewardQuestLink(Panel row, Quest quest, @Nullable QuestScreen questScreen) {
        super(row);
        this.quest = quest;
        this.questScreen = questScreen;
    }

    @Override
    public boolean checkMouseOver(int mouseX, int mouseY) {
        return getParent().isMouseOver() && mouseX < getX() + width;
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (!button.isLeft() || !isMouseOver()) {
            return false;
        }
        playClickSound();
        if (questScreen != null) {
            questScreen.open(quest, true);
        } else {
            ClientQuestFile.openGui(quest, true);
        }
        return true;
    }

    @Override
    public CursorType getCursor() {
        return CursorType.HAND;
    }
}
