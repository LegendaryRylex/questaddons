package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ModalPanel;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.PanelScrollBar;
import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.ConfigQuestObject;
import dev.ftb.mods.ftbquests.util.TextUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class QuestSearchOverlay extends ModalPanel {
    private static final int RECENT_LIMIT = 5;
    private static final int ICON_SIZE = 16;
    private static final int PAD = 3;
    private static final int SCROLL_WIDTH = 8;
    private static final int Z_LEVEL = 1000;
    private static final int FOCUSED_ZOOM = 20;
    private static final Deque<Long> RECENT = new ArrayDeque<>();
    private static final Color4I LABEL = Color4I.rgb(0xCCCCCC);
    private static final Style DETAIL = Style.EMPTY.withColor(LABEL.rgb());
    private static final Style HIT = Style.EMPTY.withColor(ChatFormatting.YELLOW);
    private static final Comparator<QuestObjectBase> DEFAULT_ORDER = Comparator.comparingInt(
                    (QuestObjectBase o) -> o.getObjectType().ordinal())
            .thenComparing(o -> o.getTitle().getString(), String.CASE_INSENSITIVE_ORDER);

    private static String query = "";
    private static @Nullable QuestSearchOverlay active;

    private final QuestScreen screen;
    private final SearchBox box;
    private final Results results;
    private final PanelScrollBar scrollBar;
    private final List<ResultRow> rows = new ArrayList<>();
    private int selected = -1;
    private int laidOutWidth;
    private int laidOutHeight;

    private QuestSearchOverlay(QuestScreen screen) {
        super(screen);
        this.screen = screen;
        this.box = new SearchBox();
        this.results = new Results();
        this.scrollBar = new PanelScrollBar(this, results);
        scrollBar.setCanAlwaysScroll(true);
        scrollBar.setScrollStep(20);
        setExtraZlevel(Z_LEVEL);
    }

    public static String query() {
        return query;
    }

    public static void clearQuery() {
        query = "";
    }

    public static void open(QuestScreen screen) {
        if (active != null && active.screen == screen && screen.anyModalPanelOpen()) {
            active.focusBox();
            return;
        }
        screen.closeQuest();
        while (screen.anyModalPanelOpen()) {
            screen.popModalPanel();
        }
        QuestSearchOverlay overlay = new QuestSearchOverlay(screen);
        active = overlay;
        screen.pushModalPanel(overlay);
        overlay.layout();
        overlay.box.setText(query, false);
        overlay.focusBox();
    }

    public static void forget(QuestScreen screen) {
        if (active != null && active.screen == screen) {
            active = null;
        }
    }

    private void focusBox() {
        box.setFocused(true);
        box.setCursorPos(box.getText().length());
        box.setSelectionPos(0);
    }

    private void close() {
        screen.closeModalPanel(this);
    }

    @Override
    public void onClosed() {
        super.onClosed();
        if (active == this) {
            active = null;
        }
    }

    private void layout() {
        laidOutWidth = screen.width;
        laidOutHeight = screen.height;
        int barRight = QuestSearchBar.right(screen);
        int fieldWidth = QuestSearchBar.fieldWidth(screen);
        int barHeight = QuestSearchBar.HEIGHT;
        int maxWidth = screen.width - 2 * QuestSearchBar.SIDE_STRIP - 2 * PAD;
        int w = Math.max(fieldWidth + 2 * PAD, Math.min(maxWidth, Mth.clamp(screen.width * 45 / 100, 300, 420)));
        int h = Math.min(screen.height - QuestSearchBar.TOP - PAD, Mth.clamp(screen.height - 60, 166, 300));
        setPosAndSize(barRight - w, QuestSearchBar.TOP, w, h);
        box.setPosAndSize(w - fieldWidth, 0, fieldWidth, barHeight);
        int listTop = barHeight + PAD;
        int listHeight = h - listTop - PAD;
        results.setPosAndSize(PAD, listTop, w - 3 * PAD - SCROLL_WIDTH, listHeight);
        scrollBar.setPosAndSize(w - PAD - SCROLL_WIDTH, listTop, SCROLL_WIDTH, listHeight);
        results.alignWidgets();
    }

    @Override
    public void addWidgets() {
        add(results);
        add(scrollBar);
        add(box);
    }

    @Override
    public void alignWidgets() {
        layout();
    }

    @Override
    public void updateMouseOver(int mouseX, int mouseY) {
        if (screen.width != laidOutWidth || screen.height != laidOutHeight) {
            layout();
        }
        super.updateMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(Key key) {
        box.setFocused(true);
        return box.keyPressed(key);
    }

    @Override
    public boolean charTyped(char c, KeyModifiers modifiers) {
        box.setFocused(true);
        return box.charTyped(c, modifiers);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        QuestSearchBar.drawPanel(graphics, x, y, w, h);
        int boxX = box.getX();
        int textY = box.getY() + (box.height - 8) / 2;
        if (rows.isEmpty() && !query.isEmpty()) {
            theme.drawString(
                    graphics,
                    Component.translatable("questaddons.gui.search.no_results"),
                    results.getX() + results.width / 2,
                    results.getY() + 10,
                    LABEL,
                    Theme.CENTERED | Theme.SHADOW);
        } else if (!query.isEmpty()) {
            Component count = Component.translatable(
                    rows.size() == 1 ? "questaddons.gui.search.result" : "questaddons.gui.search.results", rows.size());
            theme.drawString(graphics, count, boxX - 6 - theme.getStringWidth(count), textY, LABEL, Theme.SHADOW);
        }
    }

    private void moveSelection(int step) {
        if (rows.isEmpty()) {
            return;
        }
        if (selected < 0) {
            selected = step > 0 ? 0 : rows.size() - 1;
        } else {
            selected = Math.floorMod(selected + step, rows.size());
        }
        ResultRow row = rows.get(selected);
        double scroll = results.getScrollY();
        if (selected == 0) {
            results.setScrollY(0);
        } else if (row.posY < scroll) {
            results.setScrollY(row.posY);
        } else if (row.posY + row.height > scroll + results.height) {
            results.setScrollY(row.posY + row.height - results.height);
        }
    }

    private void pick(QuestObjectBase object) {
        RECENT.remove(object.id);
        RECENT.addFirst(object.id);
        while (RECENT.size() > RECENT_LIMIT) {
            RECENT.removeLast();
        }
        close();
        switch (object) {
            case Chapter chapter -> screen.selectChapter(chapter);
            case Quest quest -> {
                screen.addZoom((FOCUSED_ZOOM - screen.getZoom()) / 4.0);
                screen.selectChapter(quest.getChapter());
                screen.questPanel.scrollTo(quest.getX(), quest.getY());
                screen.open(quest, true);
            }
            case QuestLink link -> {
                screen.addZoom((FOCUSED_ZOOM - screen.getZoom()) / 4.0);
                screen.selectChapter(link.getChapter());
                screen.questPanel.scrollTo(link.getX(), link.getY());
                screen.open(link, true);
            }
            default -> {}
        }
    }

    private void addResults(Panel panel) {
        rows.clear();
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null) {
            selected = -1;
            return;
        }
        List<QuestObjectBase> found = new ArrayList<>();
        for (QuestObjectBase object : file.getAllObjects()) {
            if ((object instanceof Chapter || object instanceof Quest || object instanceof QuestLink)
                    && (file.canEdit()
                            || !(object instanceof QuestObject questObject)
                            || questObject.isSearchable(file.selfTeamData))) {
                found.add(object);
            }
        }
        found.sort(DEFAULT_ORDER);
        if (query.isEmpty()) {
            addBrowseList(panel, found);
            selected = -1;
        } else {
            found.removeIf(object -> !matches(object));
            found.sort(Comparator.comparingInt(object -> titleMatches(object) ? 0 : 1));
            found.forEach(object -> addRow(panel, object));
            selected = rows.isEmpty() ? -1 : 0;
        }
    }

    private boolean matches(QuestObjectBase object) {
        String lower = query.toLowerCase(Locale.ROOT);
        String id = object.getCodeString().toLowerCase(Locale.ROOT);
        String text =
                ConfigQuestObject.formatEntry(object).getString().toLowerCase(Locale.ROOT) + "\n" + id + "\n#" + id;
        return text.contains(lower) || QuestSearchIndex.matches(object, query, this);
    }

    private boolean titleMatches(QuestObjectBase object) {
        return object.getTitle().getString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                || QuestSearchIndex.matchesShownText(object, query, this);
    }

    private void addBrowseList(Panel panel, List<QuestObjectBase> found) {
        Map<Long, QuestObjectBase> byId = new HashMap<>();
        found.forEach(object -> byId.put(object.id, object));
        Set<Long> used = new HashSet<>();
        addUnused(panel, RECENT.stream().map(byId::get).filter(Objects::nonNull).toList(), used);
        screen.getSelectedChapter()
                .ifPresent(chapter -> addUnused(
                        panel,
                        found.stream()
                                .filter(object -> chapterOf(object) == chapter)
                                .toList(),
                        used));
        addUnused(
                panel,
                found.stream().filter(object -> object instanceof Chapter).toList(),
                used);
        addUnused(panel, found, used);
    }

    private void addUnused(Panel panel, List<QuestObjectBase> objects, Set<Long> used) {
        for (QuestObjectBase object : objects) {
            if (used.add(object.id)) {
                addRow(panel, object);
            }
        }
    }

    private void addRow(Panel panel, QuestObjectBase object) {
        ResultRow row = new ResultRow(panel, object, rows.size());
        panel.add(row);
        rows.add(row);
    }

    private static @Nullable Chapter chapterOf(QuestObjectBase object) {
        return switch (object) {
            case Quest quest -> quest.getChapter();
            case QuestLink link -> link.getChapter();
            default -> null;
        };
    }

    private class SearchBox extends TextBox {
        SearchBox() {
            super(QuestSearchOverlay.this);
        }

        @Override
        public void onTextChanged() {
            if (!getText().equals(query)) {
                query = getText();
                results.refreshWidgets();
                results.setScrollY(0);
            }
        }

        @Override
        public boolean mousePressed(MouseButton button) {
            return isMouseOver() && super.mousePressed(button);
        }

        @Override
        public boolean keyPressed(Key key) {
            if (key.esc()) {
                close();
                return true;
            }
            if (key.is(GLFW.GLFW_KEY_DOWN)) {
                moveSelection(1);
                return true;
            }
            if (key.is(GLFW.GLFW_KEY_UP)) {
                moveSelection(-1);
                return true;
            }
            if (key.enter()) {
                if (!key.modifiers.shift() && selected >= 0 && selected < rows.size()) {
                    rows.get(selected).onClicked(MouseButton.LEFT);
                }
                return true;
            }
            return super.keyPressed(key);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);
            if (getText().isEmpty()) {
                theme.drawString(
                        graphics,
                        Component.translatable("questaddons.gui.search.placeholder")
                                .withStyle(ChatFormatting.ITALIC),
                        x + 4,
                        y + (h - 8) / 2,
                        LABEL,
                        Theme.SHADOW);
            }
        }

        @Override
        public void drawTextBox(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            QuestSearchBar.drawInset(graphics, x, y, w, h);
        }
    }

    private class Results extends Panel {
        Results() {
            super(QuestSearchOverlay.this);
        }

        @Override
        public void addWidgets() {
            addResults(this);
        }

        @Override
        public void alignWidgets() {
            widgets.forEach(widget -> widget.setWidth(width));
            align(WidgetLayout.VERTICAL);
        }
    }

    private class ResultRow extends Button implements QuestObjectHolder {
        private final QuestObjectBase object;
        private final int index;
        private @Nullable Component detail;
        private int detailWidth = -1;

        ResultRow(Panel panel, QuestObjectBase object, int index) {
            super(panel, ConfigQuestObject.formatEntry(object), object.getIcon());
            this.object = object;
            this.index = index;
            setHeight(screen.getTheme().getFontHeight() * 2 + 8);
        }

        @Override
        public QuestObjectBase questaddons$object() {
            return object;
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();
            pick(object);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            if (index == selected) {
                Color4I.WHITE.withAlpha(45).draw(graphics, x, y, w, h);
            } else if (isMouseOver()) {
                Color4I.WHITE.withAlpha(30).draw(graphics, x, y, w, h);
            }
            ThemeProperties.WIDGET_BORDER.get().withAlpha(90).draw(graphics, x, y + h, w, 1);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            drawBackground(graphics, theme, x, y, w, h);
            icon.draw(graphics, x + 4, y + (h - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE);
            int textX = x + ICON_SIZE + 9;
            int textWidth = w - ICON_SIZE - 13;
            int line = theme.getFontHeight();
            int top = y + (h - line * 2 - 2) / 2 + 1;
            theme.drawString(
                    graphics, theme.trimStringToWidth(title, textWidth), textX, top, Color4I.WHITE, Theme.SHADOW);
            theme.drawString(graphics, detail(theme, textWidth), textX, top + line + 2, LABEL, Theme.SHADOW);
        }

        private Component detail(Theme theme, int width) {
            if (detail == null || detailWidth != width) {
                detail = buildDetail(theme.getFont(), width);
                detailWidth = width;
            }
            return detail;
        }

        private Component buildDetail(Font font, int width) {
            String context = context();
            String text = query.isEmpty()
                    ? subtitle()
                    : QuestSearchIndex.matchingLine(object, query, QuestSearchOverlay.this)
                            .orElseGet(this::subtitle);
            if (text.isEmpty()) {
                return Component.literal(font.plainSubstrByWidth(context, width))
                        .withStyle(DETAIL);
            }
            Component head = Component.literal(font.plainSubstrByWidth(context, width / 3) + " · ")
                    .withStyle(DETAIL);
            return Component.empty()
                    .append(head)
                    .append(SearchSnippet.excerpt(
                            text, TextIndex.tokens(query), font, width - font.width(head), DETAIL, HIT));
        }

        private String context() {
            return switch (object) {
                case Quest quest -> quest.getChapter().getTitle().getString();
                case QuestLink link ->
                    Component.translatable(
                                    "questaddons.gui.search.link",
                                    link.getChapter().getTitle())
                            .getString();
                case Chapter chapter ->
                    Component.translatable(
                                    "questaddons.gui.search.chapter_quests",
                                    chapter.getQuests().size())
                            .getString();
                default -> "";
            };
        }

        private String subtitle() {
            String subtitle =
                    switch (object) {
                        case Quest quest -> quest.getSubtitle().getString();
                        case Chapter chapter ->
                            chapter.getRawSubtitle().stream()
                                    .filter(line -> !line.isBlank())
                                    .findFirst()
                                    .map(line -> TextUtils.parseRawText(line, chapter.holderLookup())
                                            .getString())
                                    .orElse("");
                        default -> "";
                    };
            return Objects.requireNonNullElse(ChatFormatting.stripFormatting(subtitle), "")
                    .strip();
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            list.add(object.getTitle());
            list.add(Component.literal("ID: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(object.toString()).withStyle(ChatFormatting.DARK_GRAY)));
            list.add(Component.literal("Type: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(QuestObjectType.NAME_MAP
                            .getDisplayName(object.getObjectType())
                            .copy()
                            .withStyle(object.getObjectType().getColor())));
            switch (object) {
                case Quest quest -> {
                    addObject(list, quest.getChapter());
                    addRewards(list, quest);
                }
                case QuestLink link ->
                    link.getQuest().ifPresent(quest -> {
                        addObject(list, link.getChapter());
                        list.add(Component.translatable(
                                        "ftbquests.gui.linked_quest_id",
                                        Component.literal(quest.getCodeString()).withStyle(ChatFormatting.DARK_GRAY))
                                .withStyle(ChatFormatting.GRAY));
                        addObject(list, quest.getChapter());
                    });
                default -> {}
            }
        }

        private void addObject(TooltipList list, QuestObjectBase other) {
            list.add(QuestObjectType.NAME_MAP
                    .getDisplayName(other.getObjectType())
                    .copy()
                    .withStyle(ChatFormatting.GRAY)
                    .append(": ")
                    .append(other.getMutableTitle()
                            .withStyle(other.getObjectType().getColor())));
        }

        private void addRewards(TooltipList list, Quest quest) {
            if (quest.getRewards().size() == 1) {
                addObject(list, quest.getRewards().iterator().next());
            } else if (!quest.getRewards().isEmpty()) {
                list.add(Component.translatable("ftbquests.rewards").withStyle(ChatFormatting.GRAY));
                for (Reward reward : quest.getRewards()) {
                    list.add(Component.literal("  ")
                            .append(reward.getMutableTitle().withStyle(QuestObjectType.REWARD.getColor())));
                }
            }
        }
    }
}
