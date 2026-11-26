package dev.rylex.questaddons.client;

import com.mojang.datafixers.util.Either;
import dev.rylex.questaddons.QuestAddons;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = QuestAddons.MOD_ID, value = Dist.CLIENT)
public final class ItemLinkTooltip {
    private static final int ICON_SIZE = 16;
    private static final int TEXT_OFFSET = 18;
    private static final int OPAQUE = 0xFF000000;

    private static @Nullable RecipeTarget pending;

    private ItemLinkTooltip() {}

    public static void hover(Optional<Style> style) {
        pending = style.flatMap(
                        found -> FluidLink.fluid(found).map(RecipeTarget::fluid).or(() -> item(found)))
                .orElse(null);
    }

    private static Optional<RecipeTarget> item(Style style) {
        HoverEvent event = style.getHoverEvent();
        HoverEvent.ItemStackInfo info = event == null ? null : event.getValue(HoverEvent.Action.SHOW_ITEM);
        ItemStack stack = info == null ? ItemStack.EMPTY : info.getItemStack();
        return stack.isEmpty() ? Optional.empty() : Optional.of(RecipeTarget.item(stack));
    }

    @SubscribeEvent
    public static void onRegisterFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(Title.class, title -> title);
    }

    @SubscribeEvent
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        RecipeTarget target = pending;
        pending = null;
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        if (target == null || elements.isEmpty()) {
            return;
        }

        String name = target.name().getString();
        elements.getFirst()
                .left()
                .filter(title -> title.getString().startsWith(name))
                .ifPresent(title -> elements.set(
                        0, Either.right(new Title(target, Language.getInstance().getVisualOrder(title)))));
    }

    @SubscribeEvent
    public static void onTooltipPre(RenderTooltipEvent.Pre event) {
        if (isItemLink(event)) {
            event.getGraphics().setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        if (isItemLink(event)) {
            event.setBackgroundStart(event.getBackgroundStart() | OPAQUE);
            event.setBackgroundEnd(event.getBackgroundEnd() | OPAQUE);
        }
    }

    private static boolean isItemLink(RenderTooltipEvent event) {
        return event.getComponents().stream().anyMatch(Title.class::isInstance);
    }

    private record Title(RecipeTarget target, FormattedCharSequence text)
            implements TooltipComponent, ClientTooltipComponent {
        @Override
        public int getHeight() {
            return ICON_SIZE;
        }

        @Override
        public int getWidth(Font font) {
            return TEXT_OFFSET + font.width(text);
        }

        @Override
        public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
            font.drawInBatch(
                    text,
                    x + TEXT_OFFSET,
                    y + (ICON_SIZE - font.lineHeight) / 2 + 1,
                    -1,
                    true,
                    matrix,
                    buffer,
                    Font.DisplayMode.NORMAL,
                    0,
                    LightTexture.FULL_BRIGHT);
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            if (target.isFluid()) {
                target.icon().draw(graphics, x, y, ICON_SIZE, ICON_SIZE);
            } else {
                graphics.renderItem(target.stack(), x, y);
            }
        }
    }
}
