package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.config.ListConfig;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.BlankPanel;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ColorWidget;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.DependencyHighlight;
import dev.rylex.questaddons.client.DependencyPanels;
import dev.rylex.questaddons.client.FloatingQuestPanel;
import dev.rylex.questaddons.client.PreviewTargets;
import dev.rylex.questaddons.client.QuestCollapse;
import dev.rylex.questaddons.client.QuestWindows;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
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
    private Button buttonOpenDependencies;

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

        if (!button.isLeft()
                || self.getMouseY() >= panelContent.getY()
                || DependencyPanels.isOver(self, self.getMouseX(), self.getMouseY())) {
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
    private void questaddons$afterWidgets(CallbackInfo ci) {
        ViewQuestPanel self = (ViewQuestPanel) (Object) this;
        if (quest != null && panelContent != null) {
            questaddons$addCollapseButton(self);
            questaddons$addKeepButton(self);
            questaddons$replaceDependenciesButton(self);
            questaddons$replaceDependantsButton(self);
        }
        DependencyPanels.attach(self);
    }

    @Inject(method = "checkMouseOver", at = @At("RETURN"), cancellable = true)
    private void questaddons$includeDependencyPanel(int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && quest != null) {
            ViewQuestPanel self = (ViewQuestPanel) (Object) this;
            if (DependencyPanels.isOver(self, mouseX, mouseY)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void questaddons$tickDependencyPanel(CallbackInfo ci) {
        DependencyPanels.tick((ViewQuestPanel) (Object) this);
    }

    private static int questaddons$headerLeft(ViewQuestPanel self) {
        int left = self.width;
        for (Widget widget : self.getWidgets()) {
            if (widget instanceof Button
                    && widget.shouldDraw()
                    && widget.posY == 4
                    && widget.posX > self.width / 2
                    && widget.posX < left) {
                left = widget.posX;
            }
        }
        return left;
    }

    private static int questaddons$headerSize(ViewQuestPanel self) {
        for (Widget widget : self.getWidgets()) {
            if (widget instanceof Button && widget.shouldDraw() && widget.posY == 4 && widget.posX > self.width / 2) {
                return widget.width;
            }
        }
        return 0;
    }

    private void questaddons$addKeepButton(ViewQuestPanel self) {
        int size = questaddons$headerSize(self);
        if (size == 0 || ThemeProperties.FULL_SCREEN_QUEST.get(quest) == 1) {
            return;
        }

        boolean floating = self instanceof FloatingQuestPanel;
        String key = floating ? "questaddons.gui.release_window" : "questaddons.gui.keep_window";
        SimpleButton keep =
                new SimpleButton(
                        self, Component.translatable(key), floating ? Icons.LOCK : Icons.LOCK_OPEN, (button, mouse) -> {
                            if (!mouse.isLeft()) {
                                return;
                            }
                            if (self instanceof FloatingQuestPanel panel) {
                                QuestWindows.release(panel);
                            } else {
                                QuestWindows.keep(self);
                            }
                        }) {
                    @Override
                    public void addMouseOverText(TooltipList list) {
                        super.addMouseOverText(list);
                        list.add(Component.translatable(key + ".tooltip").withStyle(ChatFormatting.GRAY));
                    }
                };
        keep.setPosAndSize(questaddons$headerLeft(self) - size - 2, 4, size, size);
        self.add(keep);
    }

    private void questaddons$replaceDependantsButton(ViewQuestPanel self) {
        if (quest.getDependants().isEmpty()) {
            return;
        }

        List<Widget> widgets = self.getWidgets();
        for (int index = 0; index < widgets.size(); index++) {
            if (widgets.get(index) instanceof Button original
                    && original.getTitle().getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals("ftbquests.gui.view_dependants")) {
                Quest viewed = quest;
                SimpleButton replacement = new SimpleButton(
                        self,
                        original.getTitle(),
                        Icon.getIcon("ftbquests:textures/gui/arrow_right.png")
                                .withTint((Color4I) ThemeProperties.QUEST_VIEW_TITLE.get(viewed)),
                        (button, mouse) -> questaddons$showDependants(self, viewed));
                replacement.setPosAndSize(original.posX, original.posY, original.width, original.height);
                widgets.set(index, replacement);
                return;
            }
        }
    }

    private static void questaddons$showDependants(ViewQuestPanel self, Quest viewed) {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null) {
            return;
        }

        Collection<QuestObject> dependants = viewed.getDependants();
        List<ContextMenuItem> menu = new ArrayList<>();
        int hidden = 0;
        for (QuestObject object : dependants) {
            if (!file.canEdit() && (file.selfTeamData == null || !object.isSearchable(file.selfTeamData))) {
                hidden++;
                continue;
            }
            MutableComponent title = object.getMutableTitle();
            if (object.getQuestChapter() != null && object.getQuestChapter() != viewed.getQuestChapter()) {
                title.append(Component.literal(" [")
                        .append(object.getQuestChapter().getTitle())
                        .append("]")
                        .withStyle(ChatFormatting.GRAY));
            }
            menu.add(new ContextMenuItem(title, Color4I.empty(), button -> QuestWindows.openFrom(self, object)));
        }

        if (hidden > 0) {
            MutableComponent prefix = hidden == dependants.size() ? Component.empty() : Component.literal("+ ");
            menu.add(new ContextMenuItem(
                            prefix.append(Component.translatable("ftbquests.quest.hidden_quests_footer", hidden)),
                            Color4I.empty(),
                            null)
                    .setEnabled(false));
        }

        self.getGui().openContextMenu(menu);
    }

    private void questaddons$addCollapseButton(ViewQuestPanel self) {
        int left = questaddons$headerLeft(self);
        int size = questaddons$headerSize(self);
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

    private void questaddons$replaceDependenciesButton(ViewQuestPanel self) {
        Button original = buttonOpenDependencies;
        if (original == null || !quest.hasDependencies()) {
            return;
        }

        List<Widget> widgets = self.getWidgets();
        int index = widgets.indexOf(original);
        if (index < 0) {
            return;
        }

        Quest viewed = quest;
        Icon icon = Icon.getIcon("ftbquests:textures/gui/arrow_left.png")
                .withTint((Color4I) ThemeProperties.QUEST_VIEW_TITLE.get(viewed));
        SimpleButton replacement =
                new SimpleButton(self, original.getTitle(), icon, (button, mouse) -> {
                    if (mouse.isRight()) {
                        DependencyPanels.toggle(self);
                    } else {
                        DependencyHighlight.toggle(self);
                    }
                }) {
                    @Override
                    public void addMouseOverText(TooltipList list) {
                        super.addMouseOverText(list);
                        String hint = DependencyHighlight.isActive(self)
                                ? "questaddons.gui.view_dependencies.unhighlight"
                                : "questaddons.gui.view_dependencies.highlight";
                        list.add(Component.translatable(hint).withStyle(ChatFormatting.GRAY));
                        String panel = DependencyPanels.isOpen(self)
                                ? "questaddons.gui.view_dependencies.hide_panel"
                                : "questaddons.gui.view_dependencies.show_panel";
                        list.add(Component.translatable(panel).withStyle(ChatFormatting.GRAY));
                    }
                };
        replacement.setPosAndSize(original.posX, original.posY, original.width, original.height);
        widgets.set(index, replacement);
        buttonOpenDependencies = replacement;
    }
}
