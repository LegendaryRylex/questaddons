package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.icon.AtlasSpriteIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class TextureSize {
    private static final int PNG_HEADER = 24;
    private static final int[] SQUARE = {1, 1};
    private static final Map<Identifier, Optional<int[]>> PNG_SIZES = new ConcurrentHashMap<>();

    private TextureSize() {}

    public static Optional<int[]> of(Icon<?> icon) {
        if (icon instanceof ItemIcon) {
            return Optional.of(SQUARE);
        }
        if (icon instanceof ImageIcon image) {
            return PNG_SIZES.computeIfAbsent(image.texture, TextureSize::readPng);
        }
        if (icon instanceof AtlasSpriteIcon sprite) {
            SpriteContents contents = Minecraft.getInstance()
                    .getAtlasManager()
                    .getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS)
                    .getSprite(sprite.getSpriteId())
                    .contents();
            return Optional.of(new int[] {contents.width(), contents.height()});
        }
        return Optional.empty();
    }

    public static int[] fit(Icon<?> icon, int width, int height) {
        Optional<int[]> size = of(icon);
        if (size.isEmpty() || size.get()[0] <= 0 || size.get()[1] <= 0) {
            return new int[] {width, height};
        }
        int[] pixels = size.get();
        double scale = Math.min((double) width / pixels[0], (double) height / pixels[1]);
        return new int[] {
            Math.max(1, (int) Math.round(pixels[0] * scale)), Math.max(1, (int) Math.round(pixels[1] * scale))
        };
    }

    private static Optional<int[]> readPng(Identifier texture) {
        Optional<Resource> resource =
                Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            return Optional.empty();
        }
        try (InputStream stream = resource.get().open()) {
            byte[] header = stream.readNBytes(PNG_HEADER);
            if (header.length < PNG_HEADER) {
                return Optional.empty();
            }
            ByteBuffer buffer = ByteBuffer.wrap(header);
            int width = buffer.getInt(16);
            int height = buffer.getInt(20);
            return width > 0 && height > 0 ? Optional.of(new int[] {width, height}) : Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
