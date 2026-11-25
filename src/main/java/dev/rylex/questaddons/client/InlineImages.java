package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.client.ClientTextComponentUtils;
import dev.ftb.mods.ftblibrary.util.client.ImageComponent;
import dev.ftb.mods.ftbquests.quest.ImageClickAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public final class InlineImages {
    private static final String CLICK_PREFIX = "questaddons:image_click/";
    private static final Pattern TAG = Pattern.compile("\\{image:[^{}]*}");
    private static final int FLOAT_GAP = 4;
    private static final int GLYPH_HEIGHT = 8;
    private static final int MIN_TEXT_WIDTH = 40;

    private InlineImages() {}

    public record Run(FormattedText text, Style style, int x, int width) {}

    public record Line(int y, int height, int textY, List<Run> runs) {}

    public record Placed(Icon icon, Style style, int x, int y, int width, int height) {}

    public record Layout(List<Line> lines, List<Placed> images, int height) {
        public Optional<Style> styleAt(int x, int y) {
            for (Placed image : images) {
                if (x >= image.x()
                        && x < image.x() + image.width()
                        && y >= image.y()
                        && y < image.y() + image.height()) {
                    return Optional.of(image.style());
                }
            }
            for (Line line : lines) {
                if (y < line.y() || y >= line.y() + line.height()) {
                    continue;
                }
                for (Run run : line.runs()) {
                    if (x >= run.x() && x < run.x() + run.width()) {
                        return Optional.of(run.style());
                    }
                }
            }
            return Optional.empty();
        }
    }

    private sealed interface Piece permits TextPiece, ImagePiece {}

    private record TextPiece(String text, Style style) implements Piece {}

    private record ImagePiece(ImageComponent image, Style style) implements Piece {}

    public static boolean click(@Nullable Style style) {
        String insertion = style == null ? null : style.getInsertion();
        if (insertion == null || !insertion.startsWith(CLICK_PREFIX)) {
            return false;
        }
        ImageClickAction.fromString(insertion.substring(CLICK_PREFIX.length())).run();
        return true;
    }

    public static boolean hasImages(Component component) {
        return flatten(component).stream().anyMatch(ImagePiece.class::isInstance);
    }

    public static boolean isImageOnly(Component component) {
        int images = 0;
        for (Piece piece : flatten(component)) {
            if (piece instanceof ImagePiece) {
                images++;
            } else if (piece instanceof TextPiece text && !text.text().isBlank()) {
                return false;
            }
        }
        return images == 1;
    }

    public static Layout layout(Component component, Font font, int maxWidth, int spacing) {
        List<Piece> flow = new ArrayList<>();
        ImagePiece left = null;
        ImagePiece right = null;
        for (Piece piece : flatten(component)) {
            if (piece instanceof ImagePiece image
                    && left == null
                    && image.image().getAlign() == ImageComponent.ImageAlign.LEFT) {
                left = image;
            } else if (piece instanceof ImagePiece image
                    && right == null
                    && image.image().getAlign() == ImageComponent.ImageAlign.RIGHT) {
                right = image;
            } else {
                flow.add(piece);
            }
        }

        List<Placed> images = new ArrayList<>();
        int floatLimit = left != null && right != null ? maxWidth / 3 : maxWidth / 2;
        Placed leftFloat = left == null ? null : place(left, floatLimit, 0, 0, false, maxWidth);
        Placed rightFloat = right == null ? null : place(right, floatLimit, 0, 0, true, maxWidth);
        if (leftFloat != null) {
            images.add(leftFloat);
        }
        if (rightFloat != null) {
            images.add(rightFloat);
        }

        Wrapper wrapper = new Wrapper(font, maxWidth, spacing, leftFloat, rightFloat, images);
        for (Piece piece : flow) {
            if (piece instanceof TextPiece text) {
                wrapper.text(text.text(), text.style());
            } else if (piece instanceof ImagePiece image) {
                wrapper.image(image);
            }
        }
        wrapper.finish();

        int height = wrapper.lines.isEmpty() ? 0 : wrapper.y - (spacing - GLYPH_HEIGHT);
        if (leftFloat != null) {
            height = Math.max(height, leftFloat.height());
        }
        if (rightFloat != null) {
            height = Math.max(height, rightFloat.height());
        }
        return new Layout(List.copyOf(wrapper.lines), List.copyOf(images), Math.max(height, 0));
    }

    private static Placed place(ImagePiece piece, int limit, int x, int y, boolean right, int maxWidth) {
        int[] size = size(piece.image(), limit);
        return new Placed(
                piece.image().getImage(), imageStyle(piece), right ? maxWidth - size[0] : x, y, size[0], size[1]);
    }

    private static int[] size(ImageComponent image, int limit) {
        int width = Math.max(1, image.getWidth());
        int height = Math.max(1, image.getHeight());
        if (width > limit && limit > 0) {
            height = Math.max(1, (int) ((long) height * limit / width));
            width = limit;
        }
        return new int[] {width, height};
    }

    private static Style imageStyle(ImagePiece piece) {
        String action = piece.image().getClickAction();
        return action.isEmpty() ? piece.style() : piece.style().withInsertion(CLICK_PREFIX + action);
    }

    private static List<Piece> flatten(Component component) {
        List<Piece> out = new ArrayList<>();
        flatten(component, Style.EMPTY, out);
        return out;
    }

    private static void flatten(Component component, Style parent, List<Piece> out) {
        Style style = component.getStyle().applyTo(parent);
        if (component.getContents() instanceof ImageComponent image) {
            out.add(new ImagePiece(image, style));
        } else {
            component
                    .getContents()
                    .visit(
                            (partStyle, text) -> {
                                addText(text, partStyle, out);
                                return Optional.empty();
                            },
                            style);
        }
        for (Component sibling : component.getSiblings()) {
            flatten(sibling, style, out);
        }
    }

    private static void addText(String text, Style style, List<Piece> out) {
        Matcher matcher = TAG.matcher(text);
        int last = 0;
        while (matcher.find()) {
            Component parsed = parseTag(matcher.group());
            if (parsed == null) {
                continue;
            }
            if (matcher.start() > last) {
                out.add(new TextPiece(text.substring(last, matcher.start()), style));
            }
            flatten(parsed, style, out);
            last = matcher.end();
        }
        if (last < text.length()) {
            out.add(new TextPiece(text.substring(last), style));
        }
    }

    @Nullable
    private static Component parseTag(String tag) {
        try {
            Component parsed = ClientTextComponentUtils.parse(tag);
            List<Piece> pieces = new ArrayList<>();
            flatten(parsed, Style.EMPTY, pieces);
            return pieces.stream().anyMatch(ImagePiece.class::isInstance) ? parsed : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static final class Wrapper {
        private final Font font;
        private final int maxWidth;
        private final int spacing;
        private final @Nullable Placed leftFloat;
        private final @Nullable Placed rightFloat;
        private final List<Placed> images;
        private final List<Line> lines = new ArrayList<>();

        private final List<Run> runs = new ArrayList<>();
        private final List<ImagePiece> lineImages = new ArrayList<>();
        private final List<int[]> lineImageBoxes = new ArrayList<>();
        private final StringBuilder fragment = new StringBuilder();
        private final List<Run> word = new ArrayList<>();
        private Style fragmentStyle = Style.EMPTY;
        private int wordWidth;
        private Style spaceStyle = Style.EMPTY;
        private int pendingSpace;
        private boolean lineOpen;
        private int y;
        private int x;
        private int left;
        private int right;
        private int tallest;

        private Wrapper(
                Font font,
                int maxWidth,
                int spacing,
                @Nullable Placed leftFloat,
                @Nullable Placed rightFloat,
                List<Placed> images) {
            this.font = font;
            this.maxWidth = maxWidth;
            this.spacing = spacing;
            this.leftFloat = leftFloat;
            this.rightFloat = rightFloat;
            this.images = images;
            openLine();
        }

        private void text(String text, Style style) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    endWord();
                    pendingSpace = 0;
                    closeLine();
                    openLine();
                } else if (c == ' ') {
                    endWord();
                    if (x > left) {
                        spaceStyle = style;
                        pendingSpace += font.width(FormattedText.of(" ", style));
                    }
                } else {
                    if (!fragmentStyle.equals(style)) {
                        endFragment();
                        fragmentStyle = style;
                    }
                    fragment.append(c);
                }
            }
            endFragment();
        }

        private void image(ImagePiece image) {
            endWord();
            int[] box = size(image.image(), right - left);
            if (x > left && x + pendingSpace + box[0] > right) {
                closeLine();
                openLine();
                box = size(image.image(), right - left);
            }
            placeSpace();
            lineImages.add(image);
            lineImageBoxes.add(new int[] {x, box[0], box[1]});
            tallest = Math.max(tallest, box[1]);
            x += box[0];
        }

        private void finish() {
            endWord();
            closeLine();
        }

        private void endFragment() {
            if (fragment.isEmpty()) {
                return;
            }
            String part = fragment.toString();
            fragment.setLength(0);
            int width = font.width(FormattedText.of(part, fragmentStyle));
            word.add(new Run(FormattedText.of(part, fragmentStyle), fragmentStyle, 0, width));
            wordWidth += width;
        }

        private void endWord() {
            endFragment();
            if (word.isEmpty()) {
                return;
            }
            if (x > left && x + pendingSpace + wordWidth > right) {
                closeLine();
                openLine();
            }
            placeSpace();
            if (x == left && wordWidth > right - left) {
                splitWord();
            } else {
                for (Run part : word) {
                    addRun(part.text(), part.style(), part.width());
                }
            }
            word.clear();
            wordWidth = 0;
        }

        private void splitWord() {
            for (Run part : word) {
                String text = part.text().getString();
                Style style = part.style();
                int start = 0;
                for (int i = 0; i < text.length(); i++) {
                    int width = font.width(FormattedText.of(text.substring(start, i + 1), style));
                    if (x + width > right && i > start) {
                        String head = text.substring(start, i);
                        addRun(FormattedText.of(head, style), style, font.width(FormattedText.of(head, style)));
                        closeLine();
                        openLine();
                        start = i;
                    }
                }
                String tail = text.substring(start);
                if (!tail.isEmpty()) {
                    addRun(FormattedText.of(tail, style), style, font.width(FormattedText.of(tail, style)));
                }
            }
        }

        private void placeSpace() {
            if (pendingSpace > 0 && x > left) {
                addRun(FormattedText.of(" ", spaceStyle), spaceStyle, pendingSpace);
            }
            pendingSpace = 0;
        }

        private void addRun(FormattedText text, Style style, int width) {
            runs.add(new Run(text, style, x, width));
            x += width;
        }

        private void openLine() {
            left = leftFloat != null && y < leftFloat.height() ? leftFloat.width() + FLOAT_GAP : 0;
            right = rightFloat != null && y < rightFloat.height()
                    ? maxWidth - rightFloat.width() - FLOAT_GAP
                    : maxWidth;
            if (right - left < MIN_TEXT_WIDTH) {
                y = Math.max(y, Math.max(bottom(leftFloat), bottom(rightFloat)));
                left = 0;
                right = maxWidth;
            }
            x = left;
            tallest = 0;
            pendingSpace = 0;
            lineOpen = true;
        }

        private static int bottom(@Nullable Placed image) {
            return image == null ? 0 : image.height() + 1;
        }

        private void closeLine() {
            if (!lineOpen) {
                return;
            }
            lineOpen = false;
            int height = Math.max(spacing, tallest + 1);
            int center = y + (height - 1) / 2;
            int textY = height == spacing ? y : center - GLYPH_HEIGHT / 2;
            for (int i = 0; i < lineImages.size(); i++) {
                ImagePiece image = lineImages.get(i);
                int[] box = lineImageBoxes.get(i);
                int imageY = height == spacing ? y + (GLYPH_HEIGHT - box[2]) / 2 : center - box[2] / 2;
                images.add(new Placed(
                        image.image().getImage(), imageStyle(image), box[0], Math.max(y, imageY), box[1], box[2]));
            }
            lines.add(new Line(y, height, textY - y, List.copyOf(runs)));
            runs.clear();
            lineImages.clear();
            lineImageBoxes.clear();
            y += height;
        }
    }
}
