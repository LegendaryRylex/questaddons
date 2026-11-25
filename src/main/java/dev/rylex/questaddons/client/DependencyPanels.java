package dev.rylex.questaddons.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.rylex.questaddons.mixin.QuestAccessor;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;

public final class DependencyPanels {
    public enum Mode {
        AUTOMATIC,
        OPEN,
        CLOSED
    }

    private static final int GAP = 6;
    private static final int MIN_WIDTH = 110;
    private static final int MAX_WIDTH = 170;
    private static final int CHECK_INTERVAL = 10;
    private static final Map<ViewQuestPanel, State> STATES = new WeakHashMap<>();

    private DependencyPanels() {}

    private static final class State {
        private long questId;
        private Mode mode = Mode.AUTOMATIC;
        private boolean expanded;
        private List<QuestObject> shown = List.of();
        private int ticks;

        @Nullable
        private QuestDependencyPanel panel;
    }

    private static State state(ViewQuestPanel view, Quest quest) {
        State state = STATES.computeIfAbsent(view, key -> new State());
        if (state.questId != quest.id) {
            state.questId = quest.id;
            state.mode = Mode.AUTOMATIC;
            state.panel = null;
        }
        return state;
    }

    public static Mode mode(ViewQuestPanel view) {
        Quest quest = view.getViewedQuest();
        return quest == null ? Mode.AUTOMATIC : state(view, quest).mode;
    }

    public static void setMode(ViewQuestPanel view, Mode mode) {
        Quest quest = view.getViewedQuest();
        if (quest != null) {
            state(view, quest).mode = mode;
        }
    }

    public static boolean isOpen(ViewQuestPanel view) {
        State state = STATES.get(view);
        return state != null && state.panel != null;
    }

    public static void toggle(ViewQuestPanel view) {
        Quest quest = view.getViewedQuest();
        if (quest == null) {
            return;
        }

        State state = state(view, quest);
        state.mode = state.expanded ? Mode.CLOSED : Mode.OPEN;
        view.refreshWidgets();
        view.updateMouseOver(view.getMouseX(), view.getMouseY());
    }

    public static void attach(ViewQuestPanel view) {
        Quest quest = view.getViewedQuest();
        if (quest == null) {
            STATES.remove(view);
            return;
        }

        State state = state(view, quest);
        state.panel = null;
        TeamData data = teamData();
        state.expanded = data != null
                && quest.hasDependencies()
                && ThemeProperties.FULL_SCREEN_QUEST.get(quest) != 1
                && (state.mode == Mode.OPEN || state.mode == Mode.AUTOMATIC && hasUnmet(quest, data));
        state.shown = state.expanded ? shown(quest, data, state.mode) : List.of();
        view.setOnlyRenderWidgetsInside(!state.expanded);
        if (!state.expanded) {
            return;
        }

        int room = view.getParent().width - view.width - GAP - 10;
        int width = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, room));
        QuestDependencyPanel panel = new QuestDependencyPanel(view, quest, state.shown);
        view.add(panel);
        panel.layout(width, view.getParent().height - 10);
        panel.setPos(-width - GAP, 0);
        state.panel = panel;

        if (!(view instanceof FloatingQuestPanel)) {
            int left = width + GAP;
            int parentWidth = view.getParent().width;
            int parentHeight = view.getParent().height;
            view.setPos(
                    left + (parentWidth - view.width - left) / 2,
                    (parentHeight - Math.max(view.height, panel.height)) / 2);
        }
    }

    public static void tick(ViewQuestPanel view) {
        State state = STATES.get(view);
        Quest quest = view.getViewedQuest();
        if (state == null || quest == null || state.questId != quest.id || ++state.ticks % CHECK_INTERVAL != 0) {
            return;
        }

        TeamData data = teamData();
        if (data == null) {
            return;
        }

        boolean stale = state.mode == Mode.AUTOMATIC
                        && (state.expanded != hasUnmet(quest, data)
                                || state.expanded && !state.shown.equals(shown(quest, data, state.mode)))
                || state.panel != null && state.panel.needsRefresh();
        if (stale) {
            view.refreshWidgets();
            view.updateMouseOver(view.getMouseX(), view.getMouseY());
        }
    }

    public static boolean isOver(ViewQuestPanel view, int mouseX, int mouseY) {
        State state = STATES.get(view);
        if (state == null || state.panel == null) {
            return false;
        }

        QuestDependencyPanel panel = state.panel;
        int x = view.getX() + panel.posX;
        int y = view.getY() + panel.posY;
        return mouseX >= x && mouseY >= y && mouseX < x + panel.width && mouseY < y + panel.height;
    }

    public static int leftExtent(ViewQuestPanel view) {
        State state = STATES.get(view);
        return state == null || state.panel == null ? 0 : state.panel.width + GAP;
    }

    public static int height(ViewQuestPanel view) {
        State state = STATES.get(view);
        return state == null || state.panel == null ? 0 : state.panel.height;
    }

    static boolean isSatisfied(Quest quest, @Nullable TeamData data, QuestObject dependency) {
        if (data == null || !dependency.isValid()) {
            return false;
        }
        return ((QuestAccessor) (Object) quest)
                        .questaddons$dependencyRequirement()
                        .needCompletion()
                ? data.isCompleted(dependency)
                : data.isStarted(dependency);
    }

    @Nullable
    static TeamData teamData() {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        return file == null ? null : file.selfTeamData;
    }

    private static boolean hasUnmet(Quest quest, TeamData data) {
        return quest.hasDependencies() && !data.areDependenciesComplete(quest);
    }

    private static List<QuestObject> shown(Quest quest, TeamData data, Mode mode) {
        return quest.streamDependencies()
                .filter(dependency -> mode != Mode.AUTOMATIC || !isSatisfied(quest, data, dependency))
                .toList();
    }
}
