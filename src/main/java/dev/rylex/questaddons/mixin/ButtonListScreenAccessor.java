package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.TextBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractButtonListScreen.class, remap = false)
public interface ButtonListScreenAccessor {
    @Accessor("searchBox")
    TextBox questaddons$searchBox();
}
