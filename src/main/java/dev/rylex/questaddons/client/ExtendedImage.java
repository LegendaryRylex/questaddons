package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.util.ClientTextComponentUtils;
import dev.ftb.mods.ftblibrary.client.util.ImageComponent;
import dev.ftb.mods.ftblibrary.icon.Icon;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public final class ExtendedImage extends ImageComponent {
    private static final String VALIGN = "valign";
    private static final String KEEP_ASPECT = "keep_aspect";
    private static final String DEFAULT_SIZE = "100";

    public enum VAlign {
        TOP,
        CENTER,
        BOTTOM;

        int offset(int room) {
            return switch (this) {
                case TOP -> 0;
                case CENTER -> room / 2;
                case BOTTOM -> room;
            };
        }

        static VAlign byName(@Nullable String name) {
            if (name == null) {
                return TOP;
            }
            return switch (name.toLowerCase(Locale.ROOT)) {
                case "center", "middle" -> CENTER;
                case "bottom" -> BOTTOM;
                default -> TOP;
            };
        }
    }

    private final VAlign valign;

    private ExtendedImage(VAlign valign) {
        this.valign = valign;
    }

    public VAlign valign() {
        return valign;
    }

    public static void register() {
        ClientTextComponentUtils.addCustomParser(ExtendedImage::parse);
    }

    @Nullable
    private static Component parse(String text, Map<String, String> map) {
        if (!map.containsKey("image") || !(map.containsKey(VALIGN) || map.containsKey(KEEP_ASPECT))) {
            return null;
        }
        int width;
        int height;
        try {
            width = Integer.parseInt(map.getOrDefault("width", DEFAULT_SIZE));
            height = Integer.parseInt(map.getOrDefault("height", DEFAULT_SIZE));
        } catch (NumberFormatException e) {
            return null;
        }
        ExtendedImage image = new ExtendedImage(VAlign.byName(map.get(VALIGN)));
        Icon<?> icon = Icon.getIcon(map.get("image"));
        image.setImage(icon);
        if ("true".equals(map.get(KEEP_ASPECT))) {
            int[] fitted = TextureSize.fit(icon, width, height);
            width = fitted[0];
            height = fitted[1];
        }
        image.setWidth(width);
        image.setHeight(height);
        image.setAlign(ImageAlign.byName(map.getOrDefault("align", "center")));
        image.setFit("true".equals(map.get("fit")));
        image.setClickAction(map.getOrDefault("click_action", ""));
        MutableComponent out = MutableComponent.create(image);
        if (map.containsKey("text")) {
            out.withStyle(Style.EMPTY.withHoverEvent(
                    new HoverEvent.ShowText(ClientTextComponentUtils.parse(map.get("text")))));
        }
        return out;
    }
}
