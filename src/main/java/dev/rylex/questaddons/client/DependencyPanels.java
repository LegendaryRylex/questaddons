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

    public enum Filter {
        ALL,
        PENDING,
        COMPLETED;

        boolean matches(boolean satisfied) {
            return this == ALL || satisfied == (this == COMPLETED);
        }

        Filter step(boolean forward) {
            Filter[] all = values();
            return all[(ordinal() + (forward ? 1 : all.length - 1)) % all.length];
        }
    }

    private static final int GAP = 6;
    private static final int MIN_WIDTH = 110;
    private static final int MAX_WIDTH = 170;
    private static final int CHECK_INTERVAL = 10;
    private static final int MIN_HEIGHT = 60;
    private static final Map<ViewQuestPanel, State> STATES = new WeakHashMap<>();

    private DependencyPanels() {}

    private static final class State {
        private long questId;
        private Mode mode = Mode.AUTOMATIC;
        private boolean expanded;
        private int ticks;

        @Nullable
        private Filter filter;

        @Nullable
        private QuestDependencyPanel panel;
    }

    private static State state(ViewQuestPanel view, Quest quest) {
        State state = STATES.computeIfAbsent(view, key -> new State());
        if (state.questId != quest.id) {
            state.questId = quest.id;
            state.mode = Mode.AUTOMATIC;
            state.filter = null;
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
        state.filter = null;
        view.refreshWidgets();
        view.updateMouseOver(view.getMouseX(), view.getMouseY());
    }

    static void stepFilter(ViewQuestPanel view, boolean forward) {
        State state = STATES.get(view);
        if (state == null || state.filter == null) {
            return;
        }

        state.filter = state.filter.step(forward);
        view.refreshWidgets();
        view.updateMouseOver(view.getMouseX(), view.getMouseY());
    }

    static boolean copyView(ViewQuestPanel from, ViewQuestPanel to) {
        State source = STATES.get(from);
        Quest quest = to.getViewedQuest();
        if (source == null || quest == null || source.questId != quest.id) {
            return false;
        }

        State target = state(to, quest);
        target.mode = source.mode;
        target.filter = source.filter;
        return true;
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
                && (state.mode == Mode.OPEN || state.mode == Mode.AUTOMATIC && autoOpens(quest, data));
        view.setOnlyRenderWidgetsInside(!state.expanded);
        if (!state.expanded) {
            return;
        }

        int room = view.getParent().width - view.width - GAP - 10;
        int width = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, room));
        List<QuestObject> dependencies = quest.streamDependencies().toList();
        if (dependencies.size() <= 1) {
            state.filter = Filter.ALL;
        } else if (state.filter == null) {
            state.filter = state.mode == Mode.AUTOMATIC ? Filter.PENDING : Filter.ALL;
        }
        QuestDependencyPanel panel = new QuestDependencyPanel(view, quest, dependencies, state.filter);
        view.add(panel);
        int maxHeight = (view.getParent().height - 10) * 2 / 3;
        if (view instanceof FloatingQuestPanel) {
            maxHeight = Math.min(maxHeight, view.getParent().height - view.posY - 5);
        }
        panel.layout(width, Math.max(MIN_HEIGHT, maxHeight));
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

        boolean stale = state.mode == Mode.AUTOMATIC && state.expanded != autoOpens(quest, data)
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

    private static boolean autoOpens(Quest quest, TeamData data) {
        if (!quest.hasDependencies() || data.isCompleted(quest)) {
            return false;
        }

        List<QuestObject> dependencies = quest.streamDependencies().toList();
        long met = dependencies.stream()
                .filter(dependency -> isSatisfied(quest, data, dependency))
                .count();
        if (quest.getMinRequiredDependencies() > 0) {
            return met < Math.min(quest.getMinRequiredDependencies(), dependencies.size());
        }
        boolean onlyOne = ((QuestAccessor) (Object) quest)
                .questaddons$dependencyRequirement()
                .needOnlyOne();
        return onlyOne ? met == 0 : met < dependencies.size();
    }
}
