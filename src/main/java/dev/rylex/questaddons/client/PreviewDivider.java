package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.CursorType;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class PreviewDivider extends Widget {
    private static final Color4I IDLE = Color4I.WHITE.withAlpha(40);
    private static final Color4I ACTIVE = Color4I.WHITE.withAlpha(140);

    private final IntConsumer onDrag;
    private final Runnable onRelease;
    private boolean dragging;

    public PreviewDivider(Panel panel, IntConsumer onDrag, Runnable onRelease) {
        super(panel);
        this.onDrag = onDrag;
        this.onRelease = onRelease;
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (isMouseOver() && button.isLeft()) {
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int button, double dragX, double dragY) {
        if (dragging) {
            onDrag.accept(getMouseX());
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(MouseButton button) {
        if (dragging) {
            dragging = false;
            onRelease.run();
        }
    }

    @Override
    public CursorType getCursor() {
        return CursorType.HRESIZE;
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        Color4I color = dragging || isMouseOver() ? ACTIVE : IDLE;
        int mid = x + w / 2;
        IconHelper.renderIcon(color, graphics, mid, y, 1, h);
        int gripY = y + h / 2 - 7;
        for (int i = 0; i < 3; i++) {
            IconHelper.renderIcon(color, graphics, mid - 2, gripY + i * 6, 5, 2);
        }
    }
}
