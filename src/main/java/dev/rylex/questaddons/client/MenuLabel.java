package dev.rylex.questaddons.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public final class MenuLabel {
    private static final int MAX_NAME_WIDTH = 200;
    private static final int MAX_DETAIL_WIDTH = 100;

    private MenuLabel() {}

    public static Component of(String name, String detail) {
        Font font = Minecraft.getInstance().font;
        return Component.literal(font.plainSubstrByWidth(name, MAX_NAME_WIDTH))
                .append(Component.literal("  " + font.plainSubstrByWidth(detail, MAX_DETAIL_WIDTH))
                        .withStyle(ChatFormatting.DARK_GRAY));
    }
}
