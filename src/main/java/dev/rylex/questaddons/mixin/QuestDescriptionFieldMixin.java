package dev.rylex.questaddons.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel$QuestDescriptionField", remap = false)
public abstract class QuestDescriptionFieldMixin {
    @Inject(method = "handleCustomClickEvent", at = @At("HEAD"), cancellable = true)
    private void questaddons$runVanillaClicks(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (questaddons$run(style.getClickEvent())) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean questaddons$run(ClickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (event instanceof ClickEvent.RunCommand(String command) && player != null) {
            player.connection.sendUnattendedCommand(Commands.trimOptionalPrefix(command), minecraft.screen);
            return true;
        }
        if (event instanceof ClickEvent.CopyToClipboard(String value)) {
            minecraft.keyboardHandler.setClipboard(value);
            return true;
        }
        return false;
    }
}
