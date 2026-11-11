package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.config.ItemStackConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditStringConfigOverlay;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.DropDownMenu;
import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class InsertContext {
    private static final int PROMPT_WIDTH = 150;
    private static final int PROMPT_Z = 900;

    private final BaseScreen screen;
    private final int anchorX;
    private final int anchorY;
    private final String selection;
    private final Consumer<String> sink;
    private final Runnable cancel;

    private @Nullable EditStringConfigOverlay<String> openPrompt;

    public InsertContext(
            BaseScreen screen, int anchorX, int anchorY, String selection, Consumer<String> sink, Runnable cancel) {
        this.screen = screen;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.selection = selection;
        this.sink = sink;
        this.cancel = cancel;
    }

    public String selection() {
        return selection;
    }

    public MutableComponent label(String fallback) {
        return Component.literal(selection.isEmpty() ? fallback : selection);
    }

    public MutableComponent label(Component fallback) {
        return selection.isEmpty() ? fallback.copy() : Component.literal(selection);
    }

    public void put(String element) {
        sink.accept(element);
    }

    public void put(Component element) {
        put(ComponentJson.encode(element, FTBQuestsClient.holderLookup()));
    }

    public void menu(List<ContextMenuItem> items) {
        DropDownMenu menu = screen.openDropdownMenu(items);
        placeUnderAnchor(menu);
        menu.getWidgets().stream().filter(TextBox.class::isInstance).forEach(widget -> ((TextBox) widget)
                .setFocused(true));
    }

    public void prompt(Component title, String initial, Consumer<String> accepted) {
        StringConfig config = new StringConfig();
        config.setValue(initial);
        EditStringConfigOverlay<String> overlay =
                new EditStringConfigOverlay<>(
                        screen,
                        config,
                        ok -> {
                            openPrompt = null;
                            String value = config.getValue();
                            if (ok && value != null && !value.isBlank()) {
                                accepted.accept(value.trim());
                            } else {
                                cancel.run();
                            }
                        },
                        title) {
                    @Override
                    public void onClosed() {
                        super.onClosed();
                        if (openPrompt == this) {
                            openPrompt = null;
                            cancel.run();
                        }
                    }
                };
        overlay.setWidth(PROMPT_WIDTH);
        overlay.setExtraZlevel(PROMPT_Z);
        openPrompt = overlay;
        screen.pushModalPanel(overlay);
        placeUnderAnchor(overlay);
    }

    public boolean dismissPrompt() {
        if (openPrompt == null) {
            return false;
        }
        screen.closeModalPanel(openPrompt);
        return true;
    }

    public void pickItem(Consumer<ItemStack> picked) {
        ItemStackConfig config = new ItemStackConfig(true, false);
        config.onClicked(screen, MouseButton.LEFT, ok -> {
            screen.openGui();
            ItemStack stack = config.getValue();
            if (ok && stack != null && !stack.isEmpty()) {
                picked.accept(stack);
            } else {
                cancel.run();
            }
        });
    }

    private void placeUnderAnchor(Widget panel) {
        panel.setPos(
                fit(anchorX, panel.width, screen.getWindow().getGuiScaledWidth()) - screen.getX(),
                fit(anchorY, panel.height, screen.getWindow().getGuiScaledHeight()) - screen.getY());
    }

    private static int fit(int start, int size, int limit) {
        return Math.max(0, Math.min(start, limit - size));
    }
}
