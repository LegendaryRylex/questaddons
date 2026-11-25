package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

public final class QuestWindows {
    private static final int DRAG_THRESHOLD = 3;
    private static final int DOCK_MARGIN = 4;
    private static final Map<QuestScreen, List<FloatingQuestPanel>> FLOATING = new WeakHashMap<>();

    @Nullable
    private static ViewQuestPanel dragged;

    private static int grabX;
    private static int grabY;
    private static int pressX;
    private static int pressY;
    private static boolean moved;

    @Nullable
    private static ViewQuestPanel pendingPopOut;

    @Nullable
    private static FloatingQuestPanel pendingRaise;

    private QuestWindows() {}

    public static List<FloatingQuestPanel> floating(QuestScreen screen) {
        return FLOATING.getOrDefault(screen, List.of());
    }

    public static boolean hasFloating(BaseScreen screen) {
        List<FloatingQuestPanel> panels = FLOATING.get(screen);
        return panels != null && !panels.isEmpty();
    }

    public static boolean isOverFloating(QuestScreen screen, int mouseX, int mouseY) {
        for (FloatingQuestPanel panel : floating(screen)) {
            if (!panel.isClosed() && panel.checkMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    public static void addTo(QuestScreen screen) {
        floating(screen).forEach(screen::add);
    }

    public static void refresh(QuestScreen screen) {
        floating(screen).forEach(FloatingQuestPanel::requestRefresh);
    }

    public static void beginDrag(ViewQuestPanel panel, int mouseX, int mouseY) {
        dragged = panel;
        grabX = mouseX - panel.getX();
        grabY = mouseY - panel.getY();
        pressX = mouseX;
        pressY = mouseY;
        moved = false;
    }

    public static void endDrag() {
        dragged = null;
    }

    public static void raise(FloatingQuestPanel panel) {
        pendingRaise = panel;
    }

    public static void popOutForHighlight(ViewQuestPanel panel) {
        pendingPopOut = panel;
    }

    public static void update(QuestScreen screen) {
        prune(screen);
        applyRaise(screen);
        applyPopOut(screen);
        applyRefresh(screen);
        applyDrag(screen);
    }

    private static void prune(QuestScreen screen) {
        List<FloatingQuestPanel> panels = FLOATING.get(screen);
        if (panels == null) {
            return;
        }

        panels.removeIf(panel -> {
            Quest quest = panel.getViewedQuest();
            boolean gone = panel.isClosed() || quest == null || !quest.isValid();
            if (gone) {
                screen.getWidgets().remove(panel);
                if (dragged == panel) {
                    dragged = null;
                }
            }
            return gone;
        });
    }

    private static void applyRaise(QuestScreen screen) {
        FloatingQuestPanel panel = pendingRaise;
        if (panel == null || panel.questScreen() != screen) {
            return;
        }

        pendingRaise = null;
        List<FloatingQuestPanel> panels = FLOATING.get(screen);
        if (panels == null || panels.isEmpty() || panels.getLast() == panel || !panels.remove(panel)) {
            return;
        }

        panels.add(panel);
        screen.getWidgets().remove(panel);
        screen.getWidgets().add(panel);
    }

    private static void applyPopOut(QuestScreen screen) {
        ViewQuestPanel source = pendingPopOut;
        if (source == null || source.getGui() != screen) {
            return;
        }

        pendingPopOut = null;
        FloatingQuestPanel panel = detach(screen, source);
        if (panel == null) {
            return;
        }

        panel.moveTo(screen.getX() + screen.width - panel.width - DOCK_MARGIN, panel.getY());
        DependencyHighlight.show(panel);
    }

    private static void applyRefresh(QuestScreen screen) {
        for (FloatingQuestPanel panel : floating(screen)) {
            if (panel.takeRefresh()) {
                panel.refreshWidgets();
            }
        }
    }

    private static void applyDrag(QuestScreen screen) {
        ViewQuestPanel panel = dragged;
        if (panel == null || panel.getGui() != screen) {
            return;
        }

        int mouseX = screen.getMouseX();
        int mouseY = screen.getMouseY();
        if (!moved) {
            if (Math.abs(mouseX - pressX) < DRAG_THRESHOLD && Math.abs(mouseY - pressY) < DRAG_THRESHOLD) {
                return;
            }
            moved = true;
        }

        if (!(panel instanceof FloatingQuestPanel)) {
            panel = detach(screen, panel);
            dragged = panel;
            if (panel == null) {
                return;
            }
        }

        ((FloatingQuestPanel) panel).moveTo(mouseX - grabX, mouseY - grabY);
    }

    @Nullable
    private static FloatingQuestPanel detach(QuestScreen screen, ViewQuestPanel source) {
        Quest quest = source.getViewedQuest();
        if (quest == null) {
            return null;
        }

        FloatingQuestPanel panel = new FloatingQuestPanel(screen, quest, source.posX, source.posY);
        FLOATING.computeIfAbsent(screen, key -> new ArrayList<>()).add(panel);
        if (screen.viewQuestPanel == source) {
            screen.closeQuest();
        }

        screen.add(panel);
        panel.updateMouseOver(screen.getMouseX(), screen.getMouseY());
        return panel;
    }
}
