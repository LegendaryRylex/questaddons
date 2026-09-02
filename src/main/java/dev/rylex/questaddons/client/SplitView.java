package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.rylex.questaddons.mixin.QuestScreenAccessor;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public final class SplitView {
    private static final int DIVIDER_WIDTH = 4;
    private static final int MIN_SIDE = 60;
    private static final double MIN_FRACTION = 0.2;
    private static final double MAX_FRACTION = 0.8;

    @Nullable
    private static QuestScreen root;

    @Nullable
    private static SplitViewPane pane;

    private static SplitOrientation orientation = SplitOrientation.SIDE_BY_SIDE;
    private static double sideBySideFraction = 0.5;
    private static double stackedFraction = 0.5;
    private static double dragFraction = 0.5;
    private static boolean draggingDivider;
    private static boolean paneFocused;

    private SplitView() {}

    public static boolean isRoot(BaseScreen screen) {
        return pane != null && root == screen;
    }

    /**
     * The unmodified mapping also matches while Alt is held, so the stacked variant has to be tested
     * first.
     */
    @Nullable
    public static SplitOrientation requested(Key key) {
        if (key.matches(QuestAddonsKeys.SPLIT_VIEW_STACKED)) {
            return SplitOrientation.STACKED;
        }

        return key.matches(QuestAddonsKeys.SPLIT_VIEW) ? SplitOrientation.SIDE_BY_SIDE : null;
    }

    public static void toggle(QuestScreen screen, SplitOrientation requested) {
        if (screen instanceof SplitViewPane) {
            return;
        }

        if (pane == null) {
            orientation = requested;
            open(screen);
        } else if (orientation == requested) {
            close();
        } else {
            orientation = requested;
        }

        screen.initGui();
    }

    public static void close() {
        root = null;
        pane = null;
        paneFocused = false;
        draggingDivider = false;
    }

    private static void open(QuestScreen screen) {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null) {
            return;
        }

        SplitViewPane created = new SplitViewPane(file);
        root = screen;
        pane = created;
        paneFocused = false;
        draggingDivider = false;

        applyPaneBounds(created, screen);
        created.initGui();

        Chapter chapter = otherChapter(file, ((QuestScreenAccessor) screen).questaddons$selectedChapter());
        if (chapter != null) {
            created.selectChapter(chapter);
        }
    }

    public static void applyRootBounds(QuestScreen screen) {
        SplitViewPane current = pane;
        if (current == null || root != screen) {
            return;
        }

        if (orientation == SplitOrientation.SIDE_BY_SIDE) {
            screen.setWidth(splitPos(screen));
        } else {
            screen.setHeight(splitPos(screen));
        }

        applyPaneBounds(current, screen);
    }

    public static int rootX(BaseScreen screen, int original) {
        return isRoot(screen) ? 0 : original;
    }

    public static int rootY(BaseScreen screen, int original) {
        return isRoot(screen) ? 0 : original;
    }

    /**
     * Mirrors the per-frame sequence ScreenWrapper runs for a root screen; the pane is outside the
     * widget tree, so nothing else drives it.
     */
    public static void draw(QuestScreen screen, GuiGraphics graphics) {
        SplitViewPane current = pane;
        if (current == null || root != screen) {
            return;
        }

        applyPaneBounds(current, screen);
        current.updateGui(screen.getMouseX(), screen.getMouseY(), screen.getPartialTicks());

        Theme theme = current.getTheme();
        QuestObjectBase previous =
                QuestTheme.setFallbackQuestObject(((QuestScreenAccessor) current).questaddons$selectedChapter());
        current.draw(graphics, theme, current.getX(), current.getY(), current.width, current.height);
        current.drawForeground(graphics, theme, current.getX(), current.getY(), current.width, current.height);
        QuestTheme.setFallbackQuestObject(previous);

        int position = draggingDivider ? clampedSplit(screen, dragFraction) : splitPos(screen);
        boolean highlight = draggingDivider || overDivider(screen, mouseAlong(screen));
        Color4I color = highlight ? Color4I.WHITE : Color4I.DARK_GRAY;
        if (orientation == SplitOrientation.SIDE_BY_SIDE) {
            color.draw(graphics, position, 0, DIVIDER_WIDTH, windowHeight(screen));
        } else {
            color.draw(graphics, 0, position, windowWidth(screen), DIVIDER_WIDTH);
        }
    }

    public static void addPaneTooltip(BaseScreen screen, TooltipList list) {
        SplitViewPane current = pane;
        if (current == null || root != screen || mouseAlong(screen) < paneStart(current)) {
            return;
        }

        current.addMouseOverText(list);
    }

    public static void tick(QuestScreen screen) {
        SplitViewPane current = pane;
        if (current != null && root == screen) {
            current.tick();
        }
    }

    public static boolean mouseClicked(BaseScreen screen, double x, double y, int button) {
        SplitViewPane current = pane;
        if (current == null || root != screen) {
            return false;
        }

        double along = along(x, y);
        if (overDivider(screen, along)) {
            draggingDivider = true;
            dragFraction = fraction();
            return true;
        }

        if (along < paneStart(current)) {
            paneFocused = false;
            return false;
        }

        paneFocused = true;
        if (button == MouseButton.BACK.id) {
            goBack(current);
        } else {
            current.updateMouseOver((int) x, (int) y);
            current.mousePressed(MouseButton.get(button));
        }

        return true;
    }

    public static boolean mouseReleased(BaseScreen screen, double x, double y, int button) {
        SplitViewPane current = pane;
        QuestScreen owner = root;
        if (current == null || owner != screen) {
            return false;
        }

        if (draggingDivider) {
            draggingDivider = false;
            setFraction(dragFraction);
            owner.initGui();
            return true;
        }

        if (!paneFocused) {
            return false;
        }

        current.updateMouseOver((int) x, (int) y);
        current.mouseReleased(MouseButton.get(button));
        return true;
    }

    public static boolean mouseScrolled(BaseScreen screen, double x, double y, double dirX, double dirY) {
        SplitViewPane current = pane;
        if (current == null || root != screen || along(x, y) < paneStart(current)) {
            return false;
        }

        return current.mouseScrolled(x, y, dirX, dirY);
    }

    public static boolean mouseDragged(BaseScreen screen, double x, double y, int button, double dragX, double dragY) {
        SplitViewPane current = pane;
        if (current == null || root != screen) {
            return false;
        }

        if (draggingDivider) {
            dragFraction = clampFraction(along(x, y) / axisLength(screen));
            return true;
        }

        return paneFocused && current.mouseDragged(button, dragX, dragY);
    }

    public static boolean charTyped(BaseScreen screen, char c, int modifiers) {
        SplitViewPane current = pane;
        return current != null && root == screen && paneFocused && current.charTyped(c, new KeyModifiers(modifiers));
    }

    public static boolean keyPressed(QuestScreen screen, Key key) {
        SplitViewPane current = pane;
        if (current == null || root != screen || !paneFocused || requested(key) != null) {
            return false;
        }

        if (current.keyPressed(key)) {
            return true;
        }

        return key.backspace() && goBack(current);
    }

    public static boolean keyReleased(QuestScreen screen, Key key) {
        SplitViewPane current = pane;
        if (current == null || root != screen || !paneFocused) {
            return false;
        }

        current.keyReleased(key);
        return true;
    }

    /**
     * QuestScreen.onBack falls through to closeGui when no quest is open, which would tear down the
     * whole book from a pane gesture.
     */
    private static boolean goBack(SplitViewPane target) {
        if (!target.isViewingQuest()) {
            return false;
        }

        target.onBack();
        return true;
    }

    private static void applyPaneBounds(SplitViewPane target, BaseScreen reference) {
        int start = splitPos(reference) + DIVIDER_WIDTH;
        if (orientation == SplitOrientation.SIDE_BY_SIDE) {
            target.setBounds(start, 0, windowWidth(reference) - start, windowHeight(reference));
        } else {
            target.setBounds(0, start, windowWidth(reference), windowHeight(reference) - start);
        }
    }

    private static int paneStart(SplitViewPane target) {
        return orientation == SplitOrientation.SIDE_BY_SIDE ? target.getX() : target.getY();
    }

    private static double along(double x, double y) {
        return orientation == SplitOrientation.SIDE_BY_SIDE ? x : y;
    }

    private static double mouseAlong(BaseScreen screen) {
        return orientation == SplitOrientation.SIDE_BY_SIDE ? screen.getMouseX() : screen.getMouseY();
    }

    private static boolean overDivider(BaseScreen screen, double along) {
        int position = splitPos(screen);
        return along >= position - 1 && along < position + DIVIDER_WIDTH + 1;
    }

    private static double fraction() {
        return orientation == SplitOrientation.SIDE_BY_SIDE ? sideBySideFraction : stackedFraction;
    }

    private static void setFraction(double value) {
        if (orientation == SplitOrientation.SIDE_BY_SIDE) {
            sideBySideFraction = clampFraction(value);
        } else {
            stackedFraction = clampFraction(value);
        }
    }

    private static int splitPos(BaseScreen screen) {
        return clampedSplit(screen, fraction());
    }

    private static int clampedSplit(BaseScreen screen, double value) {
        int axis = axisLength(screen);
        return Mth.clamp((int) Math.round(axis * value), MIN_SIDE, axis - MIN_SIDE - DIVIDER_WIDTH);
    }

    private static int axisLength(BaseScreen screen) {
        return orientation == SplitOrientation.SIDE_BY_SIDE ? windowWidth(screen) : windowHeight(screen);
    }

    private static double clampFraction(double value) {
        return Mth.clamp(value, MIN_FRACTION, MAX_FRACTION);
    }

    private static int windowWidth(BaseScreen screen) {
        return screen.getWindow().getGuiScaledWidth();
    }

    private static int windowHeight(BaseScreen screen) {
        return screen.getWindow().getGuiScaledHeight();
    }

    @Nullable
    private static Chapter otherChapter(ClientQuestFile file, @Nullable Chapter current) {
        List<Chapter> chapters = file.getVisibleChapters(file.selfTeamData);
        if (chapters.isEmpty()) {
            return null;
        }

        for (Chapter chapter : chapters) {
            if (chapter != current) {
                return chapter;
            }
        }

        return chapters.get(0);
    }
}
