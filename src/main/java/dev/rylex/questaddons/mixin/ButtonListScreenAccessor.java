package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractButtonListScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractButtonListScreen.class, remap = false)
public interface ButtonListScreenAccessor {
    @Accessor("searchBox")
    TextBox questaddons$searchBox();
}
