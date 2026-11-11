package dev.rylex.questaddons.compat.guideme;

import dev.rylex.questaddons.client.DocsLink;
import java.util.List;
import net.neoforged.fml.ModList;

public final class GuideMeCompat {
    public static final String MOD_ID = "guideme";

    private GuideMeCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static List<DocsLink> links() {
        return isLoaded() ? GuideMeLinks.all() : List.of();
    }
}
