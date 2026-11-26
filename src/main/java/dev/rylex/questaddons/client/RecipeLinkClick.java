package dev.rylex.questaddons.client;

import dev.rylex.questaddons.QuestAddons;
import dev.rylex.questaddons.compat.jei.JeiCompat;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class RecipeLinkClick {
    private static final Identifier ACTION = QuestAddons.id("open_recipe");
    private static final String LINK = "link";

    private RecipeLinkClick() {}

    public static ClickEvent of(String link) {
        CompoundTag payload = new CompoundTag();
        payload.putString(LINK, link);
        return new ClickEvent.Custom(ACTION, Optional.of(payload));
    }

    public static boolean open(Style style) {
        if (style == null
                || !JeiCompat.isLoaded()
                || !(style.getClickEvent() instanceof ClickEvent.Custom(Identifier id, Optional<Tag> payload))
                || !ACTION.equals(id)
                || !(payload.orElse(null) instanceof CompoundTag tag)) {
            return false;
        }
        return tag.getString(LINK).map(JeiCompat::openRecipeLink).orElse(false);
    }
}
