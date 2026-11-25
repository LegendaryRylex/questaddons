package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.BlankPanel;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ColorWidget;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.FloatingQuestPanel;
import dev.rylex.questaddons.client.PreviewTargets;
import dev.rylex.questaddons.client.QuestCollapse;
import dev.rylex.questaddons.client.QuestWindows;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ViewQuestPanel.class, remap = false)
public abstract class ViewQuestPanelMixin {
    @Shadow
    private Quest quest;

    @Shadow
    private BlankPanel panelContent;

    @Shadow
    private BlankPanel panelTasks;

    @Shadow
    private BlankPanel panelText;

    @ModifyVariable(method = "editDescription", at = @At("STORE"), ordinal = 0)
    private ListConfig<String, StringConfig> questaddons$trackDescription(ListConfig<String, StringConfig> config) {
        PreviewTargets.register(config, quest, ((Widget) (Object) this).width);
        return config;
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$keysOnlyWhenHovered(Key key, CallbackInfo ci) {
        if ((Object) this instanceof FloatingQuestPanel floating && !floating.isMouseOver()) {
            ci.cancel();
        }
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void questaddons$swallowGestureRelease(Key key, CallbackInfo ci) {
        if (!ClickGestureGuard.isArmed() || !key.is(ClickGestureGuard.armedKey())) {
            return;
        }

        ClickGestureGuard.disarm();
        ci.cancel();
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void questaddons$scrollOnlyWhenHovered(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof FloatingQuestPanel floating && !floating.isMouseOver()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "mousePressed", at = @At("HEAD"), cancellable = true)
    private void questaddons$grabTitleBar(MouseButton button, CallbackInfoReturnable<Boolean> cir) {
        ViewQuestPanel self = (ViewQuestPanel) (Object) this;
        if (quest == null || panelContent == null || !self.isMouseOver()) {
            return;
        }

        if (self instanceof FloatingQuestPanel floating) {
            QuestWindows.raise(floating);
        }

        if (!button.isLeft() || self.getMouseY() >= panelContent.getY()) {
            return;
        }

        for (Widget widget : self.getWidgets()) {
            if (widget instanceof Button && widget.isMouseOver()) {
                return;
            }
        }

        QuestWindows.beginDrag(self, self.getMouseX(), self.getMouseY());
        cir.setReturnValue(true);
    }

    @Inject(method = "addWidgets", at = @At("TAIL"))
    private void questaddons$addCollapseButton(CallbackInfo ci) {
        if (quest == null || panelContent == null) {
            return;
        }

        ViewQuestPanel self = (ViewQuestPanel) (Object) this;
        int left = self.width;
        int size = 0;
        for (Widget widget : self.getWidgets()) {
            if (widget instanceof Button
                    && widget.shouldDraw()
                    && widget.posY == 4
                    && widget.posX > self.width / 2
                    && widget.posX < left) {
                left = widget.posX;
                size = widget.width;
            }
        }
        if (size == 0) {
            return;
        }

        boolean collapsed = QuestCollapse.isCollapsed(quest);
        if (collapsed) {
            questaddons$collapse(self);
        }

        Quest viewed = quest;
        String key = collapsed ? "questaddons.gui.expand" : "questaddons.gui.collapse";
        SimpleButton collapse =
                new SimpleButton(
                        self,
                        Component.translatable(key),
                        collapsed ? Icons.EXPAND : Icons.COLLAPSE,
                        (button, mouse) -> {
                            QuestCollapse.toggle(viewed);
                            self.refreshWidgets();
                        }) {
                    @Override
                    public void addMouseOverText(TooltipList list) {
                        super.addMouseOverText(list);
                        list.add(Component.translatable(key + ".tooltip").withStyle(ChatFormatting.GRAY));
                    }
                };
        collapse.setPosAndSize(left - size - 2, 4, size, size);
        self.add(collapse);
    }

    private void questaddons$collapse(ViewQuestPanel self) {
        if (panelTasks == null || panelText == null || ThemeProperties.FULL_SCREEN_QUEST.get(quest) == 1) {
            return;
        }

        int bottom = panelTasks.posY + panelTasks.height + 6;
        panelText.getWidgets().clear();
        panelText.setHeight(0);
        panelContent.getWidgets().removeIf(widget -> widget instanceof ColorWidget && widget.height == 1);
        for (Widget widget : panelContent.getWidgets()) {
            if (widget instanceof ColorWidget && widget.width == 1) {
                widget.setHeight(bottom);
            }
        }
        self.setHeight(panelContent.posY + bottom);
        panelContent.setHeight(bottom);
        self.setPos((self.getParent().width - self.width) / 2, (self.getParent().height - self.height) / 2);
    }

}
