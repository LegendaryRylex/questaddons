package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.Panel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Panel.class, remap = false)
public interface PanelAccessor {
    @Accessor("contentHeight")
    void questaddons$setContentHeight(int height);
}
