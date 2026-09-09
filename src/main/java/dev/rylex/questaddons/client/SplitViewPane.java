package dev.rylex.questaddons.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;

/**
 * A QuestScreen kept outside the widget tree: Widget.parent is final and Panel.add rejects a
 * foreign parent, so the owning screen must drive initGui, updateGui and draw by hand.
 */
public class SplitViewPane extends QuestScreen {
    private final ClientQuestFile questFile;
    private int paneX;
    private int paneY;

    public SplitViewPane(ClientQuestFile file) {
        super(file, null);
        questFile = file;
        setOnlyRenderWidgetsInside(true);
        setOnlyInteractWithWidgetsInside(true);
    }

    public boolean isBackedBy(ClientQuestFile file) {
        return questFile == file;
    }

    public void setBounds(int x, int y, int w, int h) {
        if (paneX == x && paneY == y && width == w && height == h) {
            return;
        }

        paneX = x;
        paneY = y;
        setSize(w, h);
        refreshWidgets();
    }

    @Override
    public int getX() {
        return paneX;
    }

    @Override
    public int getY() {
        return paneY;
    }

    @Override
    public boolean onInit() {
        return true;
    }

    @Override
    public boolean checkMouseOver(int mouseX, int mouseY) {
        return mouseX >= paneX && mouseY >= paneY && mouseX < paneX + width && mouseY < paneY + height;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClosed() {}
}
