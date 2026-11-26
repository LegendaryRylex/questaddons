package dev.rylex.questaddons.client;

import dev.rylex.questaddons.compat.jei.JeiCompat;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;

public final class RecipeLinkClick {
    private RecipeLinkClick() {}

    public static boolean open(Style style) {
        if (style == null || !JeiCompat.isLoaded()) {
            return false;
        }
        ClickEvent click = style.getClickEvent();
        return click != null
                && click.getAction() == ClickEvent.Action.OPEN_URL
                && JeiCompat.isRecipeLink(click.getValue())
                && JeiCompat.openRecipeLink(click.getValue());
    }
}
