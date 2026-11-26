package dev.rylex.questaddons.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.QuestAddons;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

public final class QuestCollapse {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<Long> COLLAPSED = new HashSet<>();

    private static boolean loaded;

    private QuestCollapse() {}

    public static boolean isCollapsed(Quest quest) {
        load();
        return COLLAPSED.contains(quest.id);
    }

    public static void toggle(Quest quest) {
        load();
        if (!COLLAPSED.remove(quest.id)) {
            COLLAPSED.add(quest.id);
        }
        save();
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve(QuestAddons.MOD_ID).resolve("collapsed_quests.json");
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path file = file();
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            for (JsonElement element :
                    JsonParser.parseString(Files.readString(file)).getAsJsonArray()) {
                COLLAPSED.add(Long.parseUnsignedLong(element.getAsString(), 16));
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read collapsed quests from {}", file, e);
        }
    }

    private static void save() {
        JsonArray array = new JsonArray();
        for (long id : COLLAPSED) {
            array.add(String.format(Locale.ROOT, "%016X", id));
        }
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, array.toString());
        } catch (IOException e) {
            LOGGER.warn("Could not save collapsed quests to {}", file, e);
        }
    }
}
