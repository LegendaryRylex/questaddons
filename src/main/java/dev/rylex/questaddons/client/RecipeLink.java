package dev.rylex.questaddons.client;

import java.util.Comparator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record RecipeLink(String value, Component category, String recipe, ItemStack catalyst) {
    public static final Comparator<RecipeLink> ORDER = Comparator.comparing(
                    (RecipeLink link) -> link.category().getString(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(RecipeLink::recipe, String.CASE_INSENSITIVE_ORDER);
}
