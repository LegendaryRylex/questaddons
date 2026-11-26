package dev.rylex.questaddons.client;

import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.ViewQuestPanel;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.rylex.questaddons.mixin.QuestScreenAccessor;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public final class DependencyHighlight {
    private static final int COLOUR = 0xFFD54F;
    private static final long PULSE_MILLIS = 1200L;

    @Nullable
    private static FloatingQuestPanel owner;

    private static long sourceId;
    private static Set<Long> questIds = Set.of();
    private static Set<Chapter> chapters = Set.of();

    private DependencyHighlight() {}

    public static void toggle(ViewQuestPanel panel) {
        Quest quest = panel.getViewedQuest();
        if (quest == null) {
            return;
        }

        if (panel instanceof FloatingQuestPanel floating) {
            if (owner == floating && sourceId == quest.id) {
                clear();
            } else {
                show(floating);
            }
        } else {
            QuestWindows.popOutForHighlight(panel);
        }
    }

    public static boolean isActive(ViewQuestPanel panel) {
        return owner == panel && isLive();
    }

    static void show(FloatingQuestPanel panel) {
        Quest quest = panel.getViewedQuest();
        ClientQuestFile file = ClientQuestFile.getInstance();
        if (quest == null || file == null) {
            return;
        }

        Set<Long> quests = new HashSet<>();
        Set<Chapter> containing = new HashSet<>();
        quest.streamDependencies().forEach(dependency -> {
            if (!file.canEdit() && !dependency.isSearchable(file.selfTeamData)) {
                return;
            }
            if (dependency instanceof Quest dependencyQuest) {
                quests.add(dependencyQuest.id);
                containing.add(dependencyQuest.getChapter());
            } else if (dependency instanceof Chapter chapter) {
                containing.add(chapter);
            }
        });

        owner = panel;
        sourceId = quest.id;
        questIds = quests;
        chapters = containing;
    }

    public static boolean isQuest(QuestScreen screen, Quest quest) {
        return isActiveOn(screen) && questIds.contains(quest.id);
    }

    public static boolean isChapter(QuestScreen screen, Chapter chapter) {
        return isActiveOn(screen) && chapters.contains(chapter) && !isSelected(screen, chapter);
    }

    public static boolean isCollapsedGroup(QuestScreen screen, ChapterGroup group) {
        if (!group.isGuiCollapsed() || !isActiveOn(screen)) {
            return false;
        }

        for (Chapter chapter : chapters) {
            if (chapter.getGroup() == group && !isSelected(screen, chapter)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasOtherChapter(QuestScreen screen) {
        if (!isActiveOn(screen)) {
            return false;
        }

        for (Chapter chapter : chapters) {
            if (!isSelected(screen, chapter)) {
                return true;
            }
        }
        return false;
    }

    public static void outlineAround(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        Color4I colour = pulse();
        GuiHelper.drawHollowRect(graphics, x - 1, y - 1, w + 2, h + 2, colour, false);
        GuiHelper.drawHollowRect(graphics, x - 2, y - 2, w + 4, h + 4, colour, false);
    }

    public static void outlineInside(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        Color4I colour = pulse();
        GuiHelper.drawHollowRect(graphics, x, y, w, h, colour, false);
        GuiHelper.drawHollowRect(graphics, x + 1, y + 1, w - 2, h - 2, colour, false);
    }

    private static Color4I pulse() {
        float phase = (System.currentTimeMillis() % PULSE_MILLIS) / (float) PULSE_MILLIS;
        float wave = (Mth.sin(phase * Mth.TWO_PI) + 1F) / 2F;
        return Color4I.rgb(COLOUR).withAlpha((int) (110 + 145 * wave));
    }

    private static boolean isActiveOn(QuestScreen screen) {
        return owner != null && owner.questScreen() == screen && isLive();
    }

    private static boolean isLive() {
        if (owner == null) {
            return false;
        }

        Quest quest = owner.getViewedQuest();
        if (owner.isClosed() || quest == null || quest.id != sourceId) {
            clear();
            return false;
        }
        return true;
    }

    private static boolean isSelected(QuestScreen screen, Chapter chapter) {
        return ((QuestScreenAccessor) screen).questaddons$selectedChapter() == chapter;
    }

    private static void clear() {
        owner = null;
        sourceId = 0L;
        questIds = Set.of();
        chapters = Set.of();
    }
}
