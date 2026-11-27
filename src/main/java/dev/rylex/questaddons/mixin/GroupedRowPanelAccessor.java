package dev.rylex.questaddons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "dev.ftb.mods.ftblibrary.ui.misc.AbstractGroupedButtonListScreen$RowPanel", remap = false)
public interface GroupedRowPanelAccessor {
    @Accessor("value")
    Object questaddons$getValue();
}
