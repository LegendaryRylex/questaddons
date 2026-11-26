package dev.rylex.questaddons.client.pin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.PanelPositioning;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientConfig;
import dev.ftb.mods.ftbquests.client.PinnedTrackerVisibility;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.rylex.questaddons.QuestAddons;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class PinnedTracker {
    private record Row(@Nullable Quest quest, @Nullable Task task, FormattedCharSequence text, int indent) {
        boolean isGap() {
            return quest == null && task == null;
        }
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final PinnedTracker INSTANCE = new PinnedTracker();
    private static final int TITLE_BAR = 13;
    private static final int TOP = 17;
    private static final int ROW = 10;
    private static final int GAP = 3;
    private static final int ICON = 8;
    private static final int QUEST_INDENT = 4;
    private static final int TASK_INDENT = 10;
    private static final int TEXT_WIDTH = 160;
    private static final int HOVER = 0x30FFFFFF;
    private static final int TITLE_COLOUR = 0xFFFFFF00;

    private final List<Row> rows = new ArrayList<>();
    private final List<Quest> quests = new ArrayList<>();
    private Component title = Component.empty();
    private int width;
    private int height;

    private boolean loaded;
    private boolean custom;
    private int customX;
    private int customY;
    private int x;
    private int y;

    private boolean dragging;
    private double grabX;
    private double grabY;

    private PinnedTracker() {}

    public static PinnedTracker get() {
        return INSTANCE;
    }

    public List<Quest> quests() {
        return quests;
    }

    public boolean hasRows() {
        return !rows.isEmpty();
    }

    public boolean isDragging() {
        return dragging;
    }

    public void clear() {
        rows.clear();
        quests.clear();
    }

    public void rebuild(List<Quest> pinned, TeamData data, boolean showChapterTitle) {
        clear();
        quests.addAll(pinned);
        Font font = Minecraft.getInstance().font;
        boolean tasks = FTBQuestsClientConfig.PINNED_VISIBILITY.get() == PinnedTrackerVisibility.ALL;
        int contentWidth = 0;
        for (int i = 0; i < pinned.size(); i++) {
            Quest quest = pinned.get(i);
            if (i > 0) {
                rows.add(new Row(null, null, FormattedCharSequence.EMPTY, 0));
            }
            FormattedText questText = FormattedText.composite(
                    font.getSplitter().headByWidth(quest.getTitle(), TEXT_WIDTH, Style.EMPTY.withBold(true)),
                    Component.literal(" ")
                            .withStyle(ChatFormatting.DARK_AQUA)
                            .append(data.getRelativeProgress(quest) + "%"));
            contentWidth = Math.max(contentWidth, add(font, quest, null, questText, QUEST_INDENT));
            if (!tasks) {
                continue;
            }
            for (Task task : quest.getTasks()) {
                if (data.isCompleted(task)) {
                    continue;
                }
                FormattedText taskText = FormattedText.composite(
                        font.getSplitter()
                                .headByWidth(
                                        task.getMutableTitle().withStyle(ChatFormatting.GRAY),
                                        TEXT_WIDTH,
                                        Style.EMPTY.applyFormat(ChatFormatting.GRAY)),
                        Component.literal(" ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(task.formatProgress(data, data.getProgress(task)))
                                .append("/")
                                .append(task.formatMaxProgress()));
                contentWidth = Math.max(contentWidth, add(font, quest, task, taskText, TASK_INDENT));
            }
        }
        title = title(showChapterTitle);
        width = Math.max(font.width(title) + 13, contentWidth + 4);
        height = TOP + 2;
        for (Row row : rows) {
            height += row.isGap() ? GAP : ROW;
        }
    }

    private int add(Font font, Quest quest, @Nullable Task task, FormattedText text, int indent) {
        FormattedCharSequence sequence = Language.getInstance().getVisualOrder(text);
        rows.add(new Row(quest, task, sequence, indent));
        return indent + ICON + 2 + font.width(sequence);
    }

    private static Component title(boolean showChapterTitle) {
        MutableComponent title = Component.translatable("ftbquests.pinned");
        if (showChapterTitle && ClientQuestFile.exists()) {
            ClientQuestFile.getInstance()
                    .getQuestScreen()
                    .flatMap(QuestScreen::getSelectedChapter)
                    .ifPresent(chapter -> title.append(": ").append(chapter.getTitle()));
        }
        return title;
    }

    private static float scale() {
        return FTBQuestsClientConfig.PINNED_QUESTS_SCALE.get().floatValue();
    }

    private void place(int screenWidth, int screenHeight) {
        load();
        float scale = scale();
        int scaledWidth = (int) (width * scale);
        int scaledHeight = (int) (height * scale);
        if (custom) {
            x = Mth.clamp(customX, 0, Math.max(0, screenWidth - scaledWidth));
            y = Mth.clamp(customY, 0, Math.max(0, screenHeight - scaledHeight));
            return;
        }
        PanelPositioning.PanelPos pos = FTBQuestsClientConfig.PINNED_QUESTS_POS
                .get()
                .getPanelPos(
                        screenWidth,
                        screenHeight,
                        scaledWidth,
                        scaledHeight,
                        FTBQuestsClientConfig.PINNED_QUESTS_INSET_X.get(),
                        FTBQuestsClientConfig.PINNED_QUESTS_INSET_Y.get());
        x = pos.x();
        y = pos.y();
    }

    public Rect2i area() {
        float scale = scale();
        return new Rect2i(x, y, (int) Math.ceil(width * scale), (int) Math.ceil(height * scale));
    }

    public boolean contains(double mouseX, double mouseY) {
        Rect2i area = area();
        return hasRows() && area.contains((int) Math.floor(mouseX), (int) Math.floor(mouseY));
    }

    private boolean inTitleBar(double mouseY) {
        return (mouseY - y) / scale() < TITLE_BAR;
    }

    private int rowAt(double mouseX, double mouseY) {
        if (!contains(mouseX, mouseY)) {
            return -1;
        }
        double localY = (mouseY - y) / scale();
        int top = TOP;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int bottom = top + (row.isGap() ? GAP : ROW);
            if (!row.isGap() && localY >= top && localY < bottom) {
                return i;
            }
            top = bottom;
        }
        return -1;
    }

    public Optional<Task> taskAt(double mouseX, double mouseY) {
        int index = rowAt(mouseX, mouseY);
        return index < 0
                ? Optional.empty()
                : Optional.ofNullable(rows.get(index).task());
    }

    public Optional<Rect2i> iconArea(double mouseX, double mouseY) {
        int index = rowAt(mouseX, mouseY);
        if (index < 0) {
            return Optional.empty();
        }
        int top = TOP;
        for (int i = 0; i < index; i++) {
            top += rows.get(i).isGap() ? GAP : ROW;
        }
        float scale = scale();
        Row row = rows.get(index);
        return Optional.of(new Rect2i(
                x + (int) (row.indent() * scale), y + (int) (top * scale), (int) Math.ceil(ICON * scale), (int)
                        Math.ceil(ICON * scale)));
    }

    public void renderHud(Minecraft minecraft, GuiGraphicsExtractor graphics) {
        if (!hasRows() || TrackerScreens.drawsTracker(minecraft.screen)) {
            return;
        }
        place(graphics.guiWidth(), graphics.guiHeight());
        draw(graphics, -1);
    }

    void renderOverScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        place(graphics.guiWidth(), graphics.guiHeight());
        graphics.nextStratum();
        draw(graphics, rowAt(mouseX, mouseY));
    }

    void drawTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        TeamData data = TrackerActions.teamData();
        int index = rowAt(mouseX, mouseY);
        List<Component> lines = List.of();
        if (contains(mouseX, mouseY) && inTitleBar(mouseY)) {
            lines = List.of(
                    Component.translatable("questaddons.gui.pinned_tracker.drag")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("questaddons.gui.pinned_tracker.reset")
                            .withStyle(ChatFormatting.GRAY));
        } else if (index >= 0 && data != null) {
            Row row = rows.get(index);
            lines = row.task() != null
                    ? TrackerOverlay.taskTooltip(row.task(), data)
                    : List.of(
                            row.quest().getTitle(),
                            Component.translatable("questaddons.gui.pinned_tracker.open")
                                    .withStyle(ChatFormatting.GRAY));
        }
        if (!lines.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lines, mouseX, mouseY);
        }
    }

    private void draw(GuiGraphicsExtractor graphics, int hovered) {
        Font font = Minecraft.getInstance().font;
        float scale = scale();
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        GuiHelper.drawHollowRect(graphics, 0, 0, width, height, Color4I.BLACK.withAlpha(100), false);
        IconHelper.renderIcon(Color4I.BLACK.withAlpha(100), graphics, 0, 0, width, height);
        IconHelper.renderIcon(Color4I.GRAY.withAlpha(50), graphics, 1, 1, width - 2, TITLE_BAR);
        IconHelper.renderIcon(Color4I.BLACK, graphics, 0, TITLE_BAR, width, 1);
        graphics.text(font, title, (width - font.width(title)) / 2, 4, TITLE_COLOUR);
        int top = TOP;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (row.isGap()) {
                top += GAP;
                continue;
            }
            if (i == hovered) {
                graphics.fill(1, top - 1, width - 1, top + ROW - 1, HOVER);
            }
            Icon<?> icon =
                    row.task() != null ? row.task().getIcon() : row.quest().getIcon();
            IconHelper.renderIcon((Icon) icon, graphics, row.indent(), top, ICON, ICON);
            graphics.text(font, row.text(), row.indent() + ICON + 2, top, 0xFFFFFFFF);
            top += ROW;
        }
        graphics.pose().popMatrix();
    }

    boolean press(double mouseX, double mouseY, int button) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        if (inTitleBar(mouseY)) {
            if (button == 0) {
                dragging = true;
                grabX = mouseX - x;
                grabY = mouseY - y;
            } else if (button == 1) {
                TrackerActions.playClick();
                custom = false;
                save();
            }
            return true;
        }
        int index = rowAt(mouseX, mouseY);
        if (index < 0 || (button != 0 && button != 1)) {
            return true;
        }
        Row row = rows.get(index);
        if (row.task() != null) {
            TrackerActions.clickTask(row.task(), button == 1);
        } else if (button == 0) {
            TrackerActions.playClick();
            ClientQuestFile.openGui(row.quest(), true);
        }
        return true;
    }

    boolean drag(double mouseX, double mouseY) {
        if (!dragging) {
            return false;
        }
        custom = true;
        customX = (int) Math.round(mouseX - grabX);
        customY = (int) Math.round(mouseY - grabY);
        x = customX;
        y = customY;
        return true;
    }

    boolean release() {
        if (!dragging) {
            return false;
        }
        dragging = false;
        customX = x;
        customY = y;
        save();
        return true;
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve(QuestAddons.MOD_ID).resolve("pinned_tracker.json");
    }

    private void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path file = file();
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            custom = root.has("x") && root.has("y");
            if (custom) {
                customX = root.get("x").getAsInt();
                customY = root.get("y").getAsInt();
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read the pinned quests position from {}", file, e);
        }
    }

    private void save() {
        JsonObject root = new JsonObject();
        if (custom) {
            root.addProperty("x", customX);
            root.addProperty("y", customY);
        }
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, root.toString());
        } catch (IOException e) {
            LOGGER.warn("Could not save the pinned quests position to {}", file, e);
        }
    }
}
