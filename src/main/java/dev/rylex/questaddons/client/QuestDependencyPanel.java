package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.BlankPanel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.PanelScrollBar;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.TextField;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.rylex.questaddons.mixin.QuestAccessor;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class QuestDependencyPanel extends Panel {
    private static final int MAX_HEIGHT = 220;
    private static final int ICON_SIZE = 10;
    private static final int TASK_SPACING = 22;

    private final ViewQuestPanel owner;
    private final Quest quest;
    private final List<QuestObject> dependencies;
    private final DependencyPanels.Filter filter;
    private List<Entry> entries = List.of();
    private int requestedWidth;
    private int requestedHeight;

    QuestDependencyPanel(
            ViewQuestPanel owner, Quest quest, List<QuestObject> dependencies, DependencyPanels.Filter filter) {
        super(owner);
        this.owner = owner;
        this.quest = quest;
        this.dependencies = List.copyOf(dependencies);
        this.filter = filter;
    }

    void layout(int width, int maxHeight) {
        requestedWidth = width;
        requestedHeight = Math.min(MAX_HEIGHT, maxHeight);
        refreshWidgets();
    }

    @Override
    public void addWidgets() {
        setSize(requestedWidth, requestedHeight);
        TextField heading = new TextField(this)
                .setMaxWidth(width - 28)
                .setText(Component.translatable("questaddons.gui.dependencies"));
        heading.setPos(5, 5);
        heading.setColor(ThemeProperties.QUEST_VIEW_TITLE.get(quest));
        add(heading);

        SimpleButton close = new SimpleButton(
                this, Component.translatable("gui.close"), ThemeProperties.CLOSE_ICON.get(quest), (button, mouse) -> {
                    if (mouse.isLeft()) {
                        DependencyPanels.toggle(owner);
                    }
                });
        close.setPosAndSize(width - 18, 3, 14, 14);
        add(close);

        entries = buildEntries();
        int contentTop = heading.posY + heading.height + 5;
        if (dependencies.size() > 1) {
            SimpleTextButton toggle = SimpleTextButton.create(
                    this,
                    filterLabel(),
                    Icon.empty(),
                    mouse -> DependencyPanels.stepFilter(owner, !mouse.isRight()),
                    requirementText().copy().withStyle(ChatFormatting.GRAY),
                    Component.translatable("questaddons.gui.dependencies.filter.tooltip"));
            toggle.setPosAndSize(4, contentTop, width - 8, 14);
            add(toggle);
            contentTop += toggle.height + 4;
        }

        BlankPanel content = new BlankPanel(this);
        content.setPosAndSize(3, contentTop, width - 14, 1);
        add(content);

        int y = 0;
        int hidden = 0;
        for (Entry entry : entries) {
            if (!filter.matches(entry.satisfied())) {
                continue;
            }
            if (!entry.visible()) {
                hidden++;
                continue;
            }
            Card card = new Card(content, entry);
            card.setPos(0, y);
            content.add(card);
            y += card.height + 5;
        }

        if (hidden > 0) {
            TextField footer = new TextField(content)
                    .setMaxWidth(content.width - 4)
                    .setText(Component.translatable("ftbquests.quest.hidden_quests_footer", hidden));
            footer.setColor(Color4I.GRAY);
            footer.setPos(2, y);
            content.add(footer);
            y += footer.height + 3;
        }

        if (y == 0) {
            TextField none = new TextField(content)
                    .setMaxWidth(content.width - 4)
                    .setText(Component.translatable("questaddons.gui.dependencies.none"));
            none.setColor(Color4I.GRAY);
            none.setPos(2, y);
            content.add(none);
            y += none.height + 3;
        }

        setHeight(Math.min(requestedHeight, contentTop + y + 4));
        content.setHeight(Math.max(1, height - contentTop - 4));
        if (y > content.height) {
            PanelScrollBar scrollBar = new PanelScrollBar(this, content);
            scrollBar.setPosAndSize(width - 9, contentTop, 6, content.height);
            add(scrollBar);
        }
    }

    private Component filterLabel() {
        long count = entries.stream()
                .filter(entry -> filter.matches(entry.satisfied()))
                .count();
        String name = filter.name().toLowerCase(Locale.ROOT);
        return Component.translatable(
                "questaddons.gui.dependencies.filter",
                Component.translatable("questaddons.gui.dependencies.filter." + name, count));
    }

    private Component requirementText() {
        if (quest.getMinRequiredDependencies() > 0) {
            return Component.translatable("ftbquests.quest.min_required_header", quest.getMinRequiredDependencies());
        }
        String id = ((QuestAccessor) (Object) quest)
                .questaddons$dependencyRequirement()
                .getId();
        return Component.translatable("ftbquests.quest.dependency_requirement." + id);
    }

    boolean needsRefresh() {
        return !entries.equals(buildEntries());
    }

    private static boolean canEdit() {
        ClientQuestFile file = ClientQuestFile.getInstance();
        return file != null && file.canEdit();
    }

    private static boolean canInspect(Quest dependency, @Nullable TeamData data) {
        return canEdit() || !dependency.hideDetailsUntilStartable() || data != null && data.canStartTasks(dependency);
    }

    private static boolean isSearchable(Quest dependency, @Nullable TeamData data) {
        return canEdit() || data != null && dependency.isSearchable(data);
    }

    private static List<Task> tasks(QuestObject dependency, @Nullable TeamData data) {
        if (dependency instanceof Quest dependencyQuest && canInspect(dependencyQuest, data)) {
            return List.copyOf(dependencyQuest.getTasks());
        }
        if (dependency instanceof Task task && canInspect(task.getQuest(), data)) {
            return List.of(task);
        }
        if (dependency instanceof Chapter chapter) {
            return chapter.getQuests().stream()
                    .filter(chapterQuest -> isSearchable(chapterQuest, data))
                    .filter(chapterQuest -> canInspect(chapterQuest, data))
                    .flatMap(chapterQuest -> chapterQuest.getTasks().stream())
                    .toList();
        }
        return List.of();
    }

    private List<Entry> buildEntries() {
        TeamData data = DependencyPanels.teamData();
        return dependencies.stream()
                .map(dependency -> {
                    boolean visible = canEdit() || data != null && dependency.isSearchable(data);
                    boolean navigable =
                            !(dependency instanceof Quest dependencyQuest) || canInspect(dependencyQuest, data);
                    if (dependency instanceof Task task) {
                        navigable = canInspect(task.getQuest(), data);
                        visible = canEdit() || navigable && isSearchable(task.getQuest(), data);
                    }
                    boolean satisfied = DependencyPanels.isSatisfied(quest, data, dependency);
                    return new Entry(
                            dependency,
                            visible,
                            navigable,
                            satisfied,
                            visible && !satisfied ? tasks(dependency, data) : List.of());
                })
                .toList();
    }

    @Override
    public void alignWidgets() {}

    @Override
    public boolean scrollPanel(double xDelta, double yDelta) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta) || isMouseOver();
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        return super.mousePressed(button) || isMouseOver();
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        IconHelper.renderIcon((Icon) ThemeProperties.QUEST_VIEW_BACKGROUND.get(quest), graphics, x, y, w, h);
        GuiHelper.drawHollowRect(graphics, x, y, w, h, ThemeProperties.QUEST_VIEW_BORDER.get(quest), false);
    }

    private record Entry(
            QuestObject dependency, boolean visible, boolean navigable, boolean satisfied, List<Task> tasks) {}

    private final class Card extends Panel {
        private final Entry entry;
        private final TextField title;
        private final int headerHeight;
        private final int taskTop;
        private final int columns;

        private Card(Panel parent, Entry entry) {
            super(parent);
            this.entry = entry;
            setWidth(parent.width);
            title = new TextField(this)
                    .setMaxWidth(width - 34)
                    .setText(entry.dependency().getTitle());
            title.setPos(19, 4);
            headerHeight = Math.max(18, title.height + 8);
            taskTop = headerHeight + 5;
            columns = Math.max(1, (width - 12) / TASK_SPACING);
            int rows = (entry.tasks().size() + columns - 1) / columns;
            setHeight(rows == 0 ? headerHeight : taskTop + rows * TASK_SPACING + 4);
        }

        @Override
        public void addWidgets() {
            add(title);
            for (int i = 0; i < entry.tasks().size(); i++) {
                Task task = entry.tasks().get(i);
                TaskButton button = new TaskButton(this, task) {
                    @Override
                    public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
                        IconHelper.renderIcon(Color4I.BLACK.withAlpha(isMouseOver() ? 110 : 55), graphics, x, y, w, h);
                    }

                    @Override
                    public boolean mousePressed(MouseButton mouse) {
                        if (!isMouseOver()) {
                            return false;
                        }
                        if (mouse.isLeft()) {
                            playClickSound();
                            QuestWindows.openFrom(owner, task);
                        }
                        return true;
                    }
                };
                button.setPosAndSize(6 + i % columns * TASK_SPACING, taskTop + i / columns * TASK_SPACING, 20, 20);
                add(button);
            }
        }

        @Override
        public void alignWidgets() {}

        @Override
        public boolean mousePressed(MouseButton button) {
            if (super.mousePressed(button)) {
                return true;
            }
            if (isMouseOver() && button.isLeft() && entry.navigable()) {
                playClickSound();
                QuestWindows.openFrom(owner, entry.dependency());
            }
            return isMouseOver();
        }

        @Override
        public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
            IconHelper.renderIcon(Color4I.GRAY.withAlpha(isMouseOver() ? 42 : 22), graphics, x, y, w, h);
            IconHelper.renderIcon(Color4I.BLACK.withAlpha(isMouseOver() ? 130 : 100), graphics, x, y, w, headerHeight);
            TeamData data = DependencyPanels.teamData();
            if (data != null) {
                IconHelper.renderIcon(entry.dependency().getProgressColor(data), graphics, x, y, 2, h);
            }
            IconHelper.renderIcon((Icon) entry.dependency().getIcon(), graphics, x + 5, y + 4, ICON_SIZE, ICON_SIZE);
            if (entry.satisfied()) {
                IconHelper.renderIcon((Icon) ThemeProperties.CHECK_ICON.get(quest), graphics, x + w - 10, y + 4, 8, 8);
            }
        }
    }
}
