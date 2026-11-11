package dev.rylex.questaddons.client;

import java.util.function.Consumer;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class HoverStyleFinder implements ActiveTextCollector {
    private static final Parameters INITIAL = new Parameters(new Matrix3x2f());

    private final Font font;
    private final int testX;
    private final int testY;
    private Parameters defaultParameters = INITIAL;
    private Style result;
    private final Consumer<Style> scanner = style -> {
        if (style.getHoverEvent() != null) {
            result = style;
        }
    };

    public HoverStyleFinder(Font font, int testX, int testY) {
        this.font = font;
        this.testX = testX;
        this.testY = testY;
    }

    @Override
    public Parameters defaultParameters() {
        return defaultParameters;
    }

    @Override
    public void defaultParameters(Parameters parameters) {
        defaultParameters = parameters;
    }

    @Override
    public void accept(TextAlignment alignment, int anchorX, int y, Parameters parameters, FormattedCharSequence text) {
        int left = alignment.calculateLeft(anchorX, font, text);
        GuiTextRenderState state = new GuiTextRenderState(
                font,
                text,
                parameters.pose(),
                left,
                y,
                ARGB.white(parameters.opacity()),
                0,
                true,
                true,
                parameters.scissor());
        ActiveTextCollector.findElementUnderCursor(state, testX, testY, scanner);
    }

    @Override
    public void acceptScrolling(
            Component message, int centerX, int left, int right, int top, int bottom, Parameters parameters) {}

    @Nullable
    public Style result() {
        return result;
    }
}
