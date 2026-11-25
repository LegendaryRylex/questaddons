package dev.rylex.questaddons.mixin;

import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.Movable;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.client.ChangeShapeForAll;
import dev.rylex.questaddons.client.ClickGestureGuard;
import dev.rylex.questaddons.client.DependencyHighlight;
import dev.rylex.questaddons.client.QuestAddonsKeys;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestButton.class, remap = false)
public abstract class QuestButtonMixin {
    @Shadow
    @Final
    protected QuestScreen questScreen;

    @Shadow
    @Final
    Quest quest;

    @Shadow
    public abstract Movable moveAndDeleteFocus();

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$grabSelection(MouseButton button, CallbackInfo ci) {
        if (!button.isLeft() || !QuestAddonsKeys.isMoveSelectionHeld()) {
            return;
        }

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null || !file.canEdit()) {
            return;
        }

        Movable focus = moveAndDeleteFocus();
        QuestScreenAccessor accessor = (QuestScreenAccessor) questScreen;
        if (!accessor.questaddons$selectedObjects().contains(focus)) {
            questScreen.toggleSelected(focus);
        }

        ((Widget) (Object) this).playClickSound();
        ClickGestureGuard.arm(QuestAddonsKeys.MOVE_SELECTION);
        accessor.questaddons$setMovingObjects(true);
        ci.cancel();
    }

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void questaddons$toggleOptional(MouseButton button, CallbackInfo ci) {
        if (!button.isRight() || !QuestAddonsKeys.isToggleOptionalHeld()) {
            return;
        }

        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null || !file.canEdit()) {
            return;
        }

        ((QuestAccessor) (Object) quest).questaddons$setOptional(!quest.isOptional());
        ((Widget) (Object) this).playClickSound();
        ClickGestureGuard.arm(QuestAddonsKeys.TOGGLE_OPTIONAL);
        EditObjectMessage.sendToServer(quest);
        ci.cancel();
    }

    @ModifyArg(
            method = "onClicked",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Ldev/ftb/mods/ftblibrary/ui/BaseScreen;openContextMenu(Ljava/util/List;)Ldev/ftb/mods/ftblibrary/ui/ContextMenu;"))
    private List<ContextMenuItem> questaddons$addChangeShapeForAll(List<ContextMenuItem> menu) {
        ChangeShapeForAll.insertInto(menu, quest, List.copyOf(questScreen.getSelectedQuests()));
        return menu;
    }

    @Inject(method = "draw", at = @At("TAIL"))
    private void questaddons$outlineDependency(
            GuiGraphics graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci) {
        if (DependencyHighlight.isQuest(questScreen, quest)) {
            DependencyHighlight.outlineAround(graphics, x, y, w, h);
        }
    }
}
