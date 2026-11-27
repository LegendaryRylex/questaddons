package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.FTBQuestsKeyMappings;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class QuestSearchBar extends Widget {
    static final int SIDE_STRIP = 20;
    static final int TOP = 1;
    static final int HEIGHT = 18;
    private static final int SLIDE_STEP = 40;

    private final QuestScreen screen;
    private int curWidth;
    private int prevWidth;

    public QuestSearchBar(QuestScreen screen) {
        super(screen);
        this.screen = screen;
        curWidth = targetWidth();
        prevWidth = curWidth;
        layout();
    }

    static int fieldWidth(QuestScreen screen) {
        return Mth.clamp(screen.width / 4, 100, 180);
    }

    static int right(QuestScreen screen) {
        return screen.width - SIDE_STRIP;
    }

    static boolean isExpanded() {
        return QuestAddonsConfig.SEARCH_BAR_EXPANDED.get();
    }

    static void toggle() {
        QuestAddonsConfig.SEARCH_BAR_EXPANDED.set(!isExpanded());
        ConfigManager.getInstance().save("ftbquests-client");
    }

    private int targetWidth() {
        return isExpanded() && !screen.isViewingQuest() ? fieldWidth(screen) : 0;
    }

    private void layout() {
        int full = fieldWidth(screen);
        setPosAndSize(right(screen) - full, TOP, full, HEIGHT);
    }

    @Override
    public void tick() {
        super.tick();
        layout();
        prevWidth = curWidth;
        int target = targetWidth();
        if (curWidth < target) {
            curWidth = Math.min(curWidth + SLIDE_STEP, target);
        } else if (curWidth > target) {
            curWidth = Math.max(curWidth - SLIDE_STEP, target);
        }
    }

    private int fieldLeft(int width) {
        return getX() + this.width - width;
    }

    private boolean overField() {
        return isMouseOver() && curWidth > 0 && getMouseX() >= fieldLeft(curWidth);
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (!overField()) {
            return false;
        }
        playClickSound();
        if (button.isRight()) {
            QuestSearchOverlay.clearQuery();
        }
        QuestSearchOverlay.open(screen);
        return true;
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (overField()) {
            list.add(Component.translatable("questaddons.gui.search"));
            QuestAddonsKeys.hint(FTBQuestsKeyMappings.KEY_GUI_SEARCH).ifPresent(list::add);
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        int width = Mth.lerpInt(
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), prevWidth, curWidth);
        if (width <= 2) {
            return;
        }
        graphics.nextStratum();
        drawField(graphics, theme, fieldLeft(width), y, width, h);
        graphics.nextStratum();
    }

    static void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon(ThemeProperties.WIDGET_BORDER.get().withAlpha(255), graphics, x + 1, y + 1, w - 2, h - 2);
        IconHelper.renderIcon(ThemeProperties.QUEST_VIEW_BACKGROUND.get(), graphics, x, y, w, h);
    }

    static void drawInset(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon(ThemeProperties.WIDGET_BACKGROUND.get(), graphics, x, y, w, h);
        GuiHelper.drawHollowRect(graphics, x, y, w, h, ThemeProperties.WIDGET_BORDER.get(), false);
    }

    private void drawField(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        drawPanel(graphics, x, y, w, h);
        drawInset(graphics, x, y, w, h);
        if (overField()) {
            IconHelper.renderIcon(Color4I.WHITE.withAlpha(30), graphics, x, y, w, h);
        }
        String query = QuestSearchOverlay.query();
        Component text = query.isEmpty()
                ? Component.translatable("questaddons.gui.search.placeholder").withStyle(ChatFormatting.ITALIC)
                : Component.literal(theme.trimStringToWidth(query, w - 8));
        graphics.enableScissor(x, y, x + w, y + h);
        theme.drawString(
                graphics,
                text,
                x + 4,
                y + (h - 8) / 2,
                query.isEmpty() ? Color4I.rgb(0xCCCCCC) : Color4I.WHITE,
                Theme.SHADOW);
        graphics.disableScissor();
    }
}
