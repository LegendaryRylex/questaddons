package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.MultilineTextBox;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.client.util.ImageComponent;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.theme.QuestTheme;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.ftb.mods.ftbquests.util.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public final class DescriptionPreview extends Widget {
    private static final String PAGEBREAK = "{@pagebreak}";
    private static final int MARGIN = 5;
    private static final int SCROLL_ROOM = 4;
    private static final int LINE = 9;
    private static final Color4I SCROLL_TRACK = Color4I.WHITE.withAlpha(30);
    private static final Color4I SCROLL_THUMB = Color4I.WHITE.withAlpha(140);

    private final Quest quest;
    private final int viewWidth;
    private final MultilineTextBox textBox;
    private final Map<String, Component> parsed = new HashMap<>();
    private final List<Piece> pieces = new ArrayList<>();
    private final List<int[]> pages = new ArrayList<>();
    private List<String> lines = List.of();
    private String lastText;
    private int lastCursorLine = -1;
    private int layoutWidth = -1;
    private int page;
    private int contentHeight;
    private double scroll;
    private long lastFlip;
    private int[] prevArrow;
    private int[] nextArrow;

    public DescriptionPreview(Panel panel, Quest quest, int viewWidth, MultilineTextBox textBox) {
        super(panel);
        this.quest = quest;
        this.viewWidth = viewWidth;
        this.textBox = textBox;
    }

    @Override
    public void tick() {
        sync();
    }

    private void sync() {
        String text = textBox.getText();
        boolean changed = !text.equals(lastText) || width != layoutWidth;
        if (!text.equals(lastText)) {
            lastText = text;
            lines = List.of(text.split("\n"));
            buildPages();
        }
        int cursorLine = cursorLine(text, textBox.cursorPos());
        if (cursorLine != lastCursorLine) {
            lastCursorLine = cursorLine;
            int target = pageOf(cursorLine);
            if (target != page) {
                page = target;
                scroll = 0;
                changed = true;
            }
        }
        if (changed) {
            rebuild();
        }
    }

    private static int cursorLine(String text, int cursor) {
        int line = 0;
        int end = Math.min(cursor, text.length());
        for (int i = 0; i < end; i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private void buildPages() {
        pages.clear();
        int start = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).equals(PAGEBREAK)) {
                pages.add(new int[] {start, i - 1});
                start = i + 1;
            }
        }
        if (start < lines.size()) {
            pages.add(new int[] {start, lines.size() - 1});
        }
        page = Mth.clamp(page, 0, Math.max(0, pages.size() - 1));
        parsed.keySet().retainAll(lines);
    }

    private int pageOf(int line) {
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i)[1] >= line) {
                return i;
            }
        }
        return Math.max(0, pages.size() - 1);
    }

    private void rebuild() {
        QuestObjectBase previous = QuestTheme.setFallbackQuestObject(quest);
        try {
            layout(getGui().getTheme());
        } finally {
            QuestTheme.setFallbackQuestObject(previous);
        }
    }

    private void layout(Theme theme) {
        pieces.clear();
        prevArrow = null;
        nextArrow = null;
        layoutWidth = width;

        int textWidth = Math.max(40, Math.min(width - MARGIN * 2 - SCROLL_ROOM, questTextWidth(theme)));
        int y = 0;

        if (!pages.isEmpty()) {
            int[] span = pages.get(page);
            for (int i = span[0]; i <= span[1] && i < lines.size(); i++) {
                Component component = parsed.computeIfAbsent(
                        lines.get(i), raw -> TextUtils.parseRawText(raw, FTBQuestsClient.holderLookup()));
                ImageComponent image = findImage(component);
                if (image != null) {
                    y += addImage(image, y, textWidth) + 1;
                } else {
                    List<FormattedText> wrapped = theme.listFormattedStringToWidth(component, textWidth);
                    for (int l = 0; l < wrapped.size(); l++) {
                        pieces.add(new TextPiece(wrapped.get(l), 0, y + l * LINE));
                    }
                    y += Math.max(1, wrapped.size()) * LINE;
                }
            }
        }

        if (pages.size() > 1) {
            y += 3;
            Component label = Component.literal((page + 1) + "/" + pages.size()).withStyle(ChatFormatting.GRAY);
            int labelWidth = theme.getStringWidth(label);
            pieces.add(new TextPiece(label, textWidth - 24 - labelWidth, y + 3));
            if (page > 0) {
                prevArrow = new int[] {textWidth - 43 - labelWidth, y, 16, 14};
                pieces.add(new IconPiece(ThemeProperties.LEFT_ARROW.get(), prevArrow[0], y, 16, 14));
            }
            if (page < pages.size() - 1) {
                nextArrow = new int[] {textWidth - 21, y, 16, 14};
                pieces.add(new IconPiece(ThemeProperties.RIGHT_ARROW.get(), nextArrow[0], y, 16, 14));
            }
            y += 14;
        }

        contentHeight = y;
        clampScroll();
    }

    private int questTextWidth(Theme theme) {
        if (viewWidth > 0) {
            return viewWidth - 6;
        }
        Component title = quest.getTitle();
        int titleWidth = Mth.clamp(widest(theme, theme.listFormattedStringToWidth(title, 500)), 150, 500);
        int w = Math.max(200, titleWidth + 54);
        if (quest.getMinWidth() > 0) {
            w = Math.max(quest.getMinWidth(), w);
        } else if (quest.getChapter().getDefaultMinWidth() > 0) {
            w = Math.max(quest.getChapter().getDefaultMinWidth(), w);
        }
        return (w % 2 == 0 ? w + 1 : w) - 6;
    }

    private static int widest(Theme theme, List<FormattedText> lines) {
        int widest = 0;
        for (FormattedText line : lines) {
            widest = Math.max(widest, theme.getStringWidth(line));
        }
        return widest;
    }

    private int addImage(ImageComponent image, int y, int width) {
        int w = image.getWidth();
        int h = image.getHeight();
        int left = 0;
        if (image.isFit() && w > 0) {
            double scale = (double) width / w;
            w = (int) (w * scale);
            h = (int) (h * scale);
        } else if (image.getAlign() == ImageComponent.ImageAlign.CENTER) {
            left = (width - w) / 2;
        } else if (image.getAlign() == ImageComponent.ImageAlign.RIGHT) {
            left = width - w;
        }
        pieces.add(new IconPiece(image.getImage(), left, y, w, h));
        return h;
    }

    @Nullable
    private static ImageComponent findImage(Component component) {
        ComponentContents contents = component.getContents();
        if (contents instanceof ImageComponent image) {
            return image;
        }
        Iterator<Component> siblings = component.getSiblings().iterator();
        if (!siblings.hasNext()) {
            return null;
        }
        Component first = siblings.next();
        return first.getContents() instanceof ImageComponent image ? image : findImage(first);
    }

    private int originX() {
        return getX() + MARGIN;
    }

    private int originY() {
        return getY() + MARGIN - (int) scroll;
    }

    private double maxScroll() {
        return Math.max(0, contentHeight - (height - MARGIN * 2));
    }

    private void clampScroll() {
        scroll = Mth.clamp(scroll, 0, maxScroll());
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        if (lastText == null || width != layoutWidth) {
            sync();
        }
        QuestObjectBase previous = QuestTheme.setFallbackQuestObject(quest);
        try {
            render(ThemeProperties.QUEST_VIEW_BACKGROUND.get(), graphics, x, y, w, h);
            graphics.enableScissor(x + 1, y + 1, x + w - 1, y + h - 1);
            Color4I text = theme.getContentColor(WidgetType.NORMAL);
            int ox = originX();
            int oy = originY();
            for (Piece piece : pieces) {
                piece.draw(graphics, theme, text, ox, oy);
            }
            graphics.disableScissor();
        } finally {
            QuestTheme.setFallbackQuestObject(previous);
        }
        drawScrollBar(graphics, x, y, w, h);
    }

    private void drawScrollBar(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        double max = maxScroll();
        if (max <= 0) {
            return;
        }
        double visible = h - MARGIN * 2;
        int track = h - 4;
        int thumb = Math.max(8, (int) (track * visible / (visible + max)));
        int thumbY = y + 2 + (int) ((track - thumb) * (scroll / max));
        render(SCROLL_TRACK, graphics, x + w - 4, y + 2, 2, track);
        render(SCROLL_THUMB, graphics, x + w - 4, thumbY, 2, thumb);
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (!isMouseOver()) {
            return false;
        }
        if (button.isLeft()) {
            int bx = getMouseX() - originX();
            int by = getMouseY() - originY();
            if (inside(prevArrow, bx, by)) {
                flip(-1);
            } else if (inside(nextArrow, bx, by)) {
                flip(1);
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        if (!isMouseOver() || yDelta == 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        double max = maxScroll();
        if ((yDelta > 0 && scroll > 0) || (yDelta < 0 && scroll < max)) {
            scroll = Mth.clamp(scroll - yDelta * LINE * 2, 0, max);
            lastFlip = now;
        } else if (now - lastFlip > 500) {
            if (flip(yDelta < 0 ? 1 : -1)) {
                lastFlip = now;
            }
        }
        return true;
    }

    private boolean flip(int direction) {
        int target = page + direction;
        if (target < 0 || target >= pages.size()) {
            return false;
        }
        page = target;
        scroll = 0;
        rebuild();
        return true;
    }

    private static boolean inside(@Nullable int[] rect, int x, int y) {
        return rect != null && x >= rect[0] && x < rect[0] + rect[2] && y >= rect[1] && y < rect[1] + rect[3];
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (!isMouseOver()) {
            return;
        }
        int bx = getMouseX() - originX();
        int by = getMouseY() - originY();
        Theme theme = getGui().getTheme();
        for (Piece piece : pieces) {
            if (piece instanceof TextPiece text && by >= text.y() && by < text.y() + LINE && bx >= text.x()) {
                HoverStyleFinder finder = new HoverStyleFinder(theme.getFont(), bx, by);
                finder.accept(text.x(), text.y(), Language.getInstance().getVisualOrder(text.text()));
                Style style = finder.result();
                if (style != null && style.getHoverEvent() != null) {
                    addHover(style.getHoverEvent(), list);
                    return;
                }
            }
        }
    }

    private static void addHover(HoverEvent hover, TooltipList list) {
        if (hover instanceof HoverEvent.ShowText show) {
            list.add(show.value());
        } else if (hover instanceof HoverEvent.ShowItem show) {
            ItemStack stack = show.item().create();
            Minecraft mc = Minecraft.getInstance();
            stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.Default.NORMAL)
                    .forEach(list::add);
        } else if (hover instanceof HoverEvent.ShowEntity show) {
            show.entity().getTooltipLines().forEach(list::add);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void render(Icon<?> icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon((Icon) icon, graphics, x, y, w, h);
    }

    private sealed interface Piece permits TextPiece, IconPiece {
        void draw(GuiGraphicsExtractor graphics, Theme theme, Color4I textColor, int ox, int oy);
    }

    private record TextPiece(FormattedText text, int x, int y) implements Piece {
        @Override
        public void draw(GuiGraphicsExtractor graphics, Theme theme, Color4I textColor, int ox, int oy) {
            theme.drawString(graphics, text, ox + x, oy + y, textColor, 0);
        }
    }

    private record IconPiece(Icon<?> icon, int x, int y, int w, int h) implements Piece {
        @Override
        public void draw(GuiGraphicsExtractor graphics, Theme theme, Color4I textColor, int ox, int oy) {
            render(icon, graphics, ox + x, oy + y, w, h);
        }
    }
}
