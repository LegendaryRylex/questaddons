package dev.rylex.questaddons.client;

import com.mojang.blaze3d.platform.Window;
import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Button;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.TextBox;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.config.EditableQuestObject;
import dev.ftb.mods.ftbquests.client.gui.SelectQuestObjectScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.util.TextUtils;
import dev.rylex.questaddons.mixin.ButtonListScreenAccessor;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class QuestSearchScreen extends SelectQuestObjectScreen<QuestObjectBase> {
    private static final int RECENT_LIMIT = 5;
    private static final int ICON_SIZE = 16;
    private static final Deque<Long> RECENT = new ArrayDeque<>();
    private static final Style DETAIL = Style.EMPTY.withColor(ChatFormatting.GRAY);
    private static final Style HIT = Style.EMPTY.withColor(ChatFormatting.YELLOW);
    private static final Comparator<QuestObjectBase> DEFAULT_ORDER = Comparator.comparingInt(
                    (QuestObjectBase o) -> o.getObjectType().ordinal())
            .thenComparing(o -> o.getTitle().getString(), String.CASE_INSENSITIVE_ORDER);

    private final EditableQuestObject<QuestObjectBase> config;
    private final ConfigCallback callback;
    private final @Nullable Chapter chapter;
    private final List<ResultRow> rows = new ArrayList<>();
    private int selected = -1;

    public QuestSearchScreen(
            EditableQuestObject<QuestObjectBase> config, ConfigCallback callback, @Nullable Chapter chapter) {
        super(config, callback);
        this.config = config;
        this.callback = callback;
        this.chapter = chapter;
        setTitle(Component.translatable("questaddons.gui.search"));
        searchBox().ghostText = I18n.get("questaddons.gui.search.placeholder");
    }

    private TextBox searchBox() {
        return ((ButtonListScreenAccessor) this).questaddons$searchBox();
    }

    private String query() {
        return searchBox().getText();
    }

    @Override
    public boolean onInit() {
        boolean init = super.onInit();
        Window window = getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();
        setWidth(Math.min(screenWidth - 20, Mth.clamp(screenWidth * 45 / 100, 300, 420)));
        setHeight(Math.min(screenHeight - 20, Mth.clamp(screenHeight - 60, 166, 300)));
        return init;
    }

    @Override
    protected Panel createTopPanel() {
        return new SearchTopPanel();
    }

    @Override
    public void addButtons(Panel panel) {
        rows.clear();
        ClientQuestFile file = ClientQuestFile.exists() ? ClientQuestFile.getInstance() : null;
        if (file == null) {
            selected = -1;
            return;
        }
        List<QuestObjectBase> found = new ArrayList<>();
        for (QuestObjectBase object : file.getAllObjects()) {
            if (config.predicate.test(object)
                    && (file.canEdit()
                            || !(object instanceof QuestObject questObject)
                            || questObject.isSearchable(file.selfTeamData))) {
                found.add(object);
            }
        }
        found.sort(DEFAULT_ORDER);
        String query = query();
        if (query.isEmpty()) {
            addBrowseSections(panel, found);
            selected = -1;
        } else {
            found.sort(Comparator.comparingInt(object -> titleMatches(object, query) ? 0 : 1));
            found.forEach(object -> addRow(panel, object));
            selected = rows.isEmpty() ? -1 : 0;
        }
    }

    private boolean titleMatches(QuestObjectBase object, String query) {
        return object.getTitle().getString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                || QuestSearchIndex.matchesShownText(object, query, this);
    }

    private void addBrowseSections(Panel panel, List<QuestObjectBase> found) {
        Map<Long, QuestObjectBase> byId = new HashMap<>();
        found.forEach(object -> byId.put(object.id, object));
        Set<Long> used = new HashSet<>();
        addSection(
                panel,
                "recent",
                RECENT.stream().map(byId::get).filter(Objects::nonNull).toList(),
                used);
        if (chapter != null) {
            addSection(
                    panel,
                    "this_chapter",
                    found.stream()
                            .filter(object -> chapterOf(object) == chapter)
                            .toList(),
                    used);
        }
        addSection(
                panel,
                "chapters",
                found.stream().filter(object -> object instanceof Chapter).toList(),
                used);
        addSection(panel, "other", found, used);
    }

    private void addSection(Panel panel, String key, List<QuestObjectBase> objects, Set<Long> used) {
        List<QuestObjectBase> fresh =
                objects.stream().filter(object -> !used.contains(object.id)).toList();
        if (fresh.isEmpty()) {
            return;
        }
        panel.add(new SectionLabel(
                panel,
                Component.translatable("questaddons.gui.search.section." + key).withStyle(ChatFormatting.GRAY)));
        for (QuestObjectBase object : fresh) {
            used.add(object.id);
            addRow(panel, object);
        }
    }

    private void addRow(Panel panel, QuestObjectBase object) {
        ResultRow row = new ResultRow(panel, object, rows.size());
        int before = panel.getWidgets().size();
        panel.add(row);
        if (panel.getWidgets().size() > before) {
            rows.add(row);
        }
    }

    private static @Nullable Chapter chapterOf(QuestObjectBase object) {
        return switch (object) {
            case Quest quest -> quest.getChapter();
            case QuestLink link -> link.getChapter();
            default -> null;
        };
    }

    @Override
    public boolean keyPressed(Key key) {
        if (key.is(GLFW.GLFW_KEY_DOWN)) {
            moveSelection(1);
            return true;
        }
        if (key.is(GLFW.GLFW_KEY_UP)) {
            moveSelection(-1);
            return true;
        }
        if ((key.is(GLFW.GLFW_KEY_ENTER) || key.is(GLFW.GLFW_KEY_KP_ENTER))
                && !key.modifiers().shift()
                && selected >= 0
                && selected < rows.size()) {
            rows.get(selected).onClicked(MouseButton.LEFT);
            return true;
        }
        return super.keyPressed(key);
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
        double scroll = mainPanel.getScrollY();
        if (selected == 0) {
            mainPanel.setScrollY(0);
        } else if (row.posY < scroll) {
            mainPanel.setScrollY(row.posY);
        } else if (row.posY + row.height > scroll + mainPanel.height) {
            mainPanel.setScrollY(row.posY + row.height - mainPanel.height);
        }
    }

    private void pick(QuestObjectBase object) {
        RECENT.remove(object.id);
        RECENT.addFirst(object.id);
        while (RECENT.size() > RECENT_LIMIT) {
            RECENT.removeLast();
        }
        callback.save(config.updateValue(object));
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        super.drawBackground(graphics, theme, x, y, w, h);
        if (rows.isEmpty() && !query().isEmpty()) {
            theme.drawString(
                    graphics,
                    Component.translatable("questaddons.gui.search.no_results"),
                    mainPanel.getX() + mainPanel.width / 2,
                    mainPanel.getY() + 10,
                    Color4I.GRAY,
                    Theme.CENTERED);
        }
    }

    private class SearchTopPanel extends ButtonListTopPanel {
        @Override
        public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);
            TextBox box = searchBox();
            if (box.isFocused() && box.getText().isEmpty()) {
                theme.drawString(
                        graphics,
                        Component.translatable("questaddons.gui.search.placeholder")
                                .withStyle(ChatFormatting.ITALIC),
                        box.getX() + 4,
                        box.getY() + (box.height - 8) / 2,
                        Color4I.GRAY,
                        0);
            }
            if (!box.getText().isEmpty() && !rows.isEmpty()) {
                Component count = Component.translatable(
                        rows.size() == 1 ? "questaddons.gui.search.result" : "questaddons.gui.search.results",
                        rows.size());
                theme.drawString(graphics, count, x + w - 20 - theme.getStringWidth(count), y + 5, Color4I.GRAY, 0);
            }
        }
    }

    private static class SectionLabel extends Widget {
        private final Component label;

        SectionLabel(Panel panel, Component label) {
            super(panel);
            this.label = label;
            setHeight(14);
        }

        @Override
        public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawString(graphics, label, x + 3, y + h - theme.getFontHeight() - 1, Color4I.GRAY, 0);
        }
    }

    private class ResultRow extends Button implements AbstractButtonListScreen.ExtendedSearch {
        private final QuestObjectBase object;
        private final int index;
        private @Nullable Component detail;
        private int detailWidth = -1;

        ResultRow(Panel panel, QuestObjectBase object, int index) {
            super(panel, EditableQuestObject.formatEntry(object), object.getIcon());
            this.object = object;
            this.index = index;
            setHeight(QuestSearchScreen.this.getTheme().getFontHeight() * 2 + 8);
        }

        @Override
        public boolean extendedSearch(String filterText) {
            String id = filterText.startsWith("#") ? filterText.substring(1) : filterText;
            return id.length() >= 5
                            && object.getCodeString().toLowerCase(Locale.ROOT).contains(id)
                    || QuestSearchIndex.matches(object, filterText, QuestSearchScreen.this);
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();
            pick(object);
        }

        @Override
        public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
            if (index == selected) {
                IconHelper.renderIcon(Color4I.WHITE.withAlpha(45), graphics, x, y, w, h);
            } else if (isMouseOver()) {
                IconHelper.renderIcon(Color4I.WHITE.withAlpha(30), graphics, x, y, w, h);
            }
            IconHelper.renderIcon(Color4I.GRAY.withAlpha(40), graphics, x, y + h, w, 1);
        }

        @Override
        public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
            drawBackground(graphics, theme, x, y, w, h);
            drawIcon(graphics, theme, x + 4, y + (h - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE);
            int textX = x + ICON_SIZE + 9;
            int textWidth = w - ICON_SIZE - 13;
            int line = theme.getFontHeight();
            int top = y + (h - line * 2 - 2) / 2 + 1;
            theme.drawString(
                    graphics, theme.trimStringToWidth(title, textWidth), textX, top, Color4I.WHITE, Theme.SHADOW);
            theme.drawString(graphics, detail(theme, textWidth), textX, top + line + 2, Color4I.GRAY, 0);
        }

        private Component detail(Theme theme, int width) {
            if (detail == null || detailWidth != width) {
                detail = buildDetail(theme.getFont(), width);
                detailWidth = width;
            }
            return detail;
        }

        private Component buildDetail(Font font, int width) {
            String query = query();
            String context = context();
            String text = query.isEmpty()
                    ? subtitle()
                    : QuestSearchIndex.matchingLine(object, query, QuestSearchScreen.this)
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
