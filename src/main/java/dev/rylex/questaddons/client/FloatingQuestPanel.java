package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public final class FloatingQuestPanel extends ViewQuestPanel {
    private final QuestScreen screen;
    private int floatX;
    private int floatY;
    private boolean closed;
    private boolean refreshPending;

    public FloatingQuestPanel(QuestScreen screen, Quest quest, int x, int y) {
        super(screen);
        this.screen = screen;
        this.floatX = x;
        this.floatY = y;
        setViewedQuest(quest);
    }

    public QuestScreen questScreen() {
        return screen;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
    }

    public void requestRefresh() {
        refreshPending = true;
    }

    boolean takeRefresh() {
        boolean pending = refreshPending;
        refreshPending = false;
        return pending;
    }

    public void moveTo(int x, int y) {
        floatX = x - screen.getX();
        floatY = y - screen.getY();
        applyPosition();
    }

    @Override
    public void onClosed() {}

    @Override
    public void addWidgets() {
        super.addWidgets();
        if (getViewedQuest() != null) {
            applyPosition();
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        graphics.nextStratum();
        super.draw(graphics, theme, x, y, w, h);
        graphics.nextStratum();
    }

    private void applyPosition() {
        int left = DependencyPanels.leftExtent(this);
        int groupHeight = Math.max(height, DependencyPanels.height(this));
        floatX = Mth.clamp(floatX, left, Math.max(left, screen.width - width));
        floatY = Mth.clamp(floatY, 0, Math.max(0, screen.height - groupHeight));
        setPos(floatX, floatY);
    }
}
