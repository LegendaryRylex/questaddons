package dev.rylex.questaddons.client;

import com.mojang.datafixers.util.Either;
import dev.rylex.questaddons.QuestAddons;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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

@EventBusSubscriber(modid = QuestAddons.MOD_ID, value = Dist.CLIENT)
public final class ItemLinkTooltip {
    private static final int ICON_SIZE = 16;
    private static final int TEXT_OFFSET = 18;

    private static ItemStack pending = ItemStack.EMPTY;

    private ItemLinkTooltip() {}

    public static void hover(Optional<Style> style) {
        pending = style.map(ItemLinkTooltip::item).orElse(ItemStack.EMPTY);
    }

    private static ItemStack item(Style style) {
        return style.getHoverEvent() instanceof HoverEvent.ShowItem(var item) ? item.create() : ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onRegisterFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(Title.class, title -> title);
    }

    @SubscribeEvent
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = pending;
        pending = ItemStack.EMPTY;
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        if (stack.isEmpty() || elements.isEmpty()) {
            return;
        }

        String name = stack.getHoverName().getString();
        elements.getFirst()
                .left()
                .filter(title -> title.getString().startsWith(name))
                .ifPresent(title -> elements.set(
                        0, Either.right(new Title(stack, Language.getInstance().getVisualOrder(title)))));
    }

    private record Title(ItemStack stack, FormattedCharSequence text)
            implements TooltipComponent, ClientTooltipComponent {
        @Override
        public int getHeight(Font font) {
            return ICON_SIZE;
        }

        @Override
        public int getWidth(Font font) {
            return TEXT_OFFSET + font.width(text);
        }

        @Override
        public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
            graphics.text(font, text, x + TEXT_OFFSET, y + (ICON_SIZE - font.lineHeight) / 2 + 1, -1, true);
        }

        @Override
        public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
            graphics.item(stack, x, y);
        }
    }
}
