package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

public final class QuestWindows {
    private static final int DRAG_THRESHOLD = 3;
    private static final int DOCK_MARGIN = 4;
    private static final int CASCADE = 18;
    private static final int MAX_CASCADE_STEPS = 8;
    private static final Map<QuestScreen, List<FloatingQuestPanel>> FLOATING = new WeakHashMap<>();
    private static List<SavedWindow> saved = List.of();

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

    @Nullable
    private static ViewQuestPanel pendingKeep;

    @Nullable
    private static FloatingQuestPanel pendingRelease;

    @Nullable
    private static PendingOpen pendingOpen;

    private record PendingOpen(ViewQuestPanel source, QuestObject target) {}

    private record SavedWindow(long questId, int x, int y, DependencyPanels.Mode mode) {}

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
        if (!FLOATING.containsKey(screen)) {
            restore(screen);
        }
        floating(screen).forEach(screen::add);
    }

    public static void save(QuestScreen screen) {
        List<SavedWindow> windows = new ArrayList<>();
        for (FloatingQuestPanel panel : floating(screen)) {
            Quest quest = panel.getViewedQuest();
            if (!panel.isClosed() && quest != null) {
                windows.add(new SavedWindow(quest.id, panel.posX, panel.posY, DependencyPanels.mode(panel)));
            }
        }
        saved = List.copyOf(windows);
    }

    private static void restore(QuestScreen screen) {
        List<FloatingQuestPanel> panels = new ArrayList<>();
        FLOATING.put(screen, panels);
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null) {
            return;
        }

        for (SavedWindow window : saved) {
            if (file.get(window.questId()) instanceof Quest quest && quest.isValid()) {
                FloatingQuestPanel panel = new FloatingQuestPanel(screen, quest, window.x(), window.y());
                if (window.mode() != DependencyPanels.Mode.AUTOMATIC) {
                    DependencyPanels.setMode(panel, window.mode());
                    panel.refreshWidgets();
                }
                panels.add(panel);
            }
        }
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

    public static void openFrom(ViewQuestPanel source, QuestObject target) {
        pendingOpen = new PendingOpen(source, target);
    }

    public static void keep(ViewQuestPanel panel) {
        pendingKeep = panel;
    }

    public static void release(FloatingQuestPanel panel) {
        pendingRelease = panel;
    }

    public static void update(QuestScreen screen) {
        prune(screen);
        applyRaise(screen);
        applyPopOut(screen);
        applyKeep(screen);
        applyRelease(screen);
        applyOpen(screen);
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

    private static void applyKeep(QuestScreen screen) {
        ViewQuestPanel source = pendingKeep;
        if (source == null || source.getGui() != screen) {
            return;
        }

        pendingKeep = null;
        if (!(source instanceof FloatingQuestPanel)) {
            detach(screen, source);
        }
    }

    private static void applyRelease(QuestScreen screen) {
        FloatingQuestPanel panel = pendingRelease;
        if (panel == null || panel.questScreen() != screen) {
            return;
        }

        pendingRelease = null;
        Quest quest = panel.getViewedQuest();
        panel.close();
        prune(screen);
        if (quest != null && quest.isValid()) {
            screen.viewQuest(quest);
        }
    }

    private static void applyOpen(QuestScreen screen) {
        PendingOpen open = pendingOpen;
        if (open == null || open.source().getGui() != screen) {
            return;
        }

        pendingOpen = null;
        ViewQuestPanel source = open.source();
        if (!(source instanceof FloatingQuestPanel)) {
            source = detach(screen, source);
            if (source == null) {
                return;
            }
        }

        if (open.target() instanceof Chapter chapter) {
            screen.selectChapter(chapter);
            return;
        }

        Quest quest = questOf(open.target());
        if (quest == null || !quest.isValid() || quest == source.getViewedQuest()) {
            return;
        }

        for (FloatingQuestPanel panel : floating(screen)) {
            if (!panel.isClosed() && panel.getViewedQuest() == quest) {
                pendingRaise = panel;
                applyRaise(screen);
                return;
            }
        }

        int x = source.posX + CASCADE;
        int y = source.posY + CASCADE;
        for (int step = 0; step < MAX_CASCADE_STEPS && isTaken(screen, x, y); step++) {
            x += CASCADE;
            y += CASCADE;
        }

        FloatingQuestPanel panel = new FloatingQuestPanel(screen, quest, x, y);
        FLOATING.computeIfAbsent(screen, key -> new ArrayList<>()).add(panel);
        screen.add(panel);
        panel.updateMouseOver(screen.getMouseX(), screen.getMouseY());
    }

    private static boolean isTaken(QuestScreen screen, int x, int y) {
        for (FloatingQuestPanel panel : floating(screen)) {
            if (!panel.isClosed() && Math.abs(panel.posX - x) < CASCADE / 2 && Math.abs(panel.posY - y) < CASCADE / 2) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Quest questOf(QuestObject target) {
        if (target instanceof Quest quest) {
            return quest;
        }
        if (target instanceof Task task) {
            return task.getQuest();
        }
        if (target instanceof QuestLink link) {
            return link.getQuest().orElse(null);
        }
        return null;
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
        if (DependencyPanels.copyView(source, panel)) {
            panel.refreshWidgets();
        }
        FLOATING.computeIfAbsent(screen, key -> new ArrayList<>()).add(panel);
        if (screen.viewQuestPanel == source) {
            screen.closeQuest();
        }

        screen.add(panel);
        panel.updateMouseOver(screen.getMouseX(), screen.getMouseY());
        return panel;
    }
}
