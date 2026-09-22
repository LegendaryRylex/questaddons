package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ModalPanel;
import java.util.Deque;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BaseScreen.class, remap = false)
public interface BaseScreenAccessor {
    @Accessor("modalPanels")
    Deque<ModalPanel> questaddons$modalPanels();
}
