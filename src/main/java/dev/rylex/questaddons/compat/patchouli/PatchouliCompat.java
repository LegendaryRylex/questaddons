package dev.rylex.questaddons.compat.patchouli;

import dev.rylex.questaddons.client.DocsLink;
import java.util.List;
import net.neoforged.fml.ModList;

public final class PatchouliCompat {
    public static final String MOD_ID = "patchouli";

    private PatchouliCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static List<DocsLink> links() {
        return isLoaded() ? PatchouliLinks.all() : List.of();
    }
}
