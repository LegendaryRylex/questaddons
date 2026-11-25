package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChapterPanel.class, remap = false)
public interface ChapterPanelAccessor {
    @Accessor("expanded")
    boolean questaddons$expanded();
}
