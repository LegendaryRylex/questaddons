package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;

public final class FloatingQuestPanel extends ViewQuestPanel {
    private static final String CLOSE_KEY = "gui.close";

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
        Quest quest = getViewedQuest();
        if (quest != null) {
            replaceCloseButton(quest);
            applyPosition();
        }
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (getViewedQuest() != null && isMouseOver()) {
            QuestWindows.raise(this);
        }
        return super.mousePressed(button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return isMouseOver() && super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyReleased(Key key) {
        return isMouseOver() && super.keyReleased(key);
    }

    private void replaceCloseButton(Quest quest) {
        List<Widget> widgets = getWidgets();
        for (int index = 0; index < widgets.size(); index++) {
            Widget original = widgets.get(index);
            if (original instanceof Button button
                    && button.getTitle().getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals(CLOSE_KEY)) {
                SimpleTextButton replacement =
                        new SimpleTextButton(this, Component.translatable(CLOSE_KEY), (Icon<?>)
                                ThemeProperties.CLOSE_ICON.get(quest)) {
                            @Override
                            public void onClicked(MouseButton mouse) {
                                playClickSound();
                                close();
                            }

                            @Override
                            public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
                                drawIcon(graphics, theme, x + 1, y + 1, w - 2, h - 2);
                            }
                        };
                replacement.setPosAndSize(original.posX, original.posY, original.width, original.height);
                widgets.set(index, replacement);
                return;
            }
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
