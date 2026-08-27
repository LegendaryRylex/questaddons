package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.net.ForceSaveMessage;
import java.io.File;
import java.nio.file.Path;
import java.util.Calendar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SaveQuests {
    private static final Logger LOGGER = LoggerFactory.getLogger(SaveQuests.class);
    private static final String SAVE_DIR = "local/ftbquests/saved/";

    private SaveQuests() {}

    public static boolean saveOnServer() {
        if (!canEdit()) {
            return false;
        }

        Play2ServerNetworking.send(ForceSaveMessage.INSTANCE);
        return true;
    }

    public static boolean downloadQuestFiles() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (!canEdit() || player == null) {
            return false;
        }

        ClientQuestFile file = ClientQuestFile.getInstance();
        try {
            File dir = new File(minecraft.gameDirectory, SAVE_DIR + timestamp()).getCanonicalFile();
            Path path = dir.toPath();
            file.writeDataFull(path, file.holderLookup());
            file.getTranslationManager().saveToFile(file, path.resolve("lang"), true);

            String shown = "."
                    + dir.getPath()
                            .replace(minecraft.gameDirectory.getCanonicalFile().getAbsolutePath(), "");
            player.sendSystemMessage(Component.translatable("ftbquests.gui.saved_as_file", shown)
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenFile(shown))));
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to download quest files", e);
            return false;
        }
    }

    private static boolean canEdit() {
        return ClientQuestFile.getInstance() != null
                && ClientQuestFile.getInstance().canEdit();
    }

    private static String timestamp() {
        Calendar now = Calendar.getInstance();
        return "%04d-%02d-%02d-%02d-%02d-%02d"
                .formatted(
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH) + 1,
                        now.get(Calendar.DAY_OF_MONTH),
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND));
    }
}
