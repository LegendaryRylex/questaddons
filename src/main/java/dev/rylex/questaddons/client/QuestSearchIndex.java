package dev.rylex.questaddons.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.util.TextUtils;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class QuestSearchIndex {
    private static final int MIN_QUERY_LENGTH = 2;
    private static @Nullable QuestSearchIndex current;
    private static @Nullable Object checkedFor;

    private final ClientQuestFile file;
    private final List<QuestObjectBase> objects;
    private final List<Fingerprint> fingerprints;
    private final Map<Long, Integer> docs;
    private final TextIndex shown;
    private final TextIndex details;
    private final String[][] lines;
    private @Nullable String lastQuery;
    private BitSet lastMatches = new BitSet();
    private BitSet lastShown = new BitSet();

    private QuestSearchIndex(ClientQuestFile file) {
        this.file = file;
        this.objects = new ArrayList<>(file.getAllObjects());
        this.fingerprints = new ArrayList<>(objects.size());
        this.docs = new HashMap<>(objects.size() * 2);
        this.lines = new String[objects.size()][];
        TextIndex.Builder shownBuilder = new TextIndex.Builder().reserve(objects.size());
        TextIndex.Builder detailsBuilder = new TextIndex.Builder().reserve(objects.size());
        for (int doc = 0; doc < objects.size(); doc++) {
            QuestObjectBase object = objects.get(doc);
            Fingerprint fingerprint = Fingerprint.of(object);
            fingerprints.add(fingerprint);
            docs.put(object.id, doc);
            shownBuilder.add(doc, fingerprint.title().getString());
            switch (object) {
                case Quest quest -> {
                    shownBuilder.add(doc, quest.getSubtitle().getString());
                    List<String> text = new ArrayList<>();
                    for (Component line : quest.getDescription()) {
                        String plain = line.getString();
                        detailsBuilder.add(doc, plain);
                        String stripped = ChatFormatting.stripFormatting(plain).strip();
                        if (!stripped.isEmpty() && !stripped.startsWith("{")) {
                            text.add(stripped);
                        }
                    }
                    lines[doc] = text.toArray(String[]::new);
                }
                case Chapter chapter -> {
                    for (String line : chapter.getRawSubtitle()) {
                        shownBuilder.add(
                                doc,
                                TextUtils.parseRawText(line, chapter.holderLookup())
                                        .getString());
                    }
                }
                default -> {}
            }
        }
        this.shown = shownBuilder.build();
        this.details = detailsBuilder.build();
    }

    public static boolean matches(QuestObjectBase object, String query, Object screen) {
        QuestSearchIndex index = indexFor(query, screen);
        if (index == null) {
            return false;
        }
        Integer doc = index.docs.get(object.id);
        return doc != null && index.matchesFor(query).get(doc);
    }

    public static boolean matchesShownText(QuestObjectBase object, String query, Object screen) {
        QuestSearchIndex index = indexFor(query, screen);
        if (index == null) {
            return false;
        }
        Integer doc = index.docs.get(object.id);
        if (doc == null) {
            return false;
        }
        index.matchesFor(query);
        return index.lastShown.get(doc);
    }

    public static Optional<String> matchingLine(QuestObjectBase object, String query, Object screen) {
        QuestSearchIndex index = indexFor(query, screen);
        if (index == null || !(object instanceof Quest quest)) {
            return Optional.empty();
        }
        Integer doc = index.docs.get(object.id);
        if (doc == null || index.lines[doc] == null || !index.file.canEdit() && !index.descriptionVisible(quest)) {
            return Optional.empty();
        }
        List<String> terms = TextIndex.tokens(query);
        for (String line : index.lines[doc]) {
            if (SearchSnippet.firstHit(line, terms) >= 0) {
                return Optional.of(line);
            }
        }
        return Optional.empty();
    }

    private static @Nullable QuestSearchIndex indexFor(String query, Object screen) {
        ClientQuestFile file = ClientQuestFile.INSTANCE;
        if (file == null || query.strip().length() < MIN_QUERY_LENGTH) {
            return null;
        }
        QuestSearchIndex index = current;
        if (index == null || index.file != file || checkedFor != screen && index.isStale()) {
            index = new QuestSearchIndex(file);
            current = index;
        }
        checkedFor = screen;
        return index;
    }

    private BitSet matchesFor(String query) {
        if (!query.equals(lastQuery)) {
            lastQuery = query;
            List<String> terms = TextIndex.tokens(query);
            lastShown = terms.isEmpty() ? new BitSet() : shown.matchAll(terms);
            lastMatches = evaluate(terms);
        }
        return lastMatches;
    }

    private BitSet evaluate(List<String> terms) {
        if (terms.isEmpty()) {
            return new BitSet();
        }
        BitSet onlyShown = shown.matchAll(terms);
        BitSet anyField = new BitSet(objects.size());
        anyField.set(0, objects.size());
        for (String term : terms) {
            BitSet hits = shown.matchPrefix(term);
            hits.or(details.matchPrefix(term));
            anyField.and(hits);
        }
        anyField.andNot(onlyShown);
        if (!file.canEdit()) {
            for (int doc = anyField.nextSetBit(0); doc >= 0; doc = anyField.nextSetBit(doc + 1)) {
                if (!(objects.get(doc) instanceof Quest quest) || !descriptionVisible(quest)) {
                    anyField.clear(doc);
                }
            }
        }
        onlyShown.or(anyField);
        return onlyShown;
    }

    private boolean descriptionVisible(Quest quest) {
        TeamData team = file.selfTeamData;
        if (quest.getHideTextUntilComplete().get(quest.getChapter().isHideTextUntilComplete())
                && (team == null || !team.isCompleted(quest))) {
            return false;
        }
        return !quest.hideDetailsUntilStartable() || team != null && team.canStartTasks(quest);
    }

    private boolean isStale() {
        if (file.getAllObjects().size() != objects.size()) {
            return true;
        }
        int doc = 0;
        for (QuestObjectBase object : file.getAllObjects()) {
            if (objects.get(doc) != object || !fingerprints.get(doc).matches(Fingerprint.of(object))) {
                return true;
            }
            doc++;
        }
        return false;
    }

    private record Fingerprint(
            Component title,
            @Nullable Object subtitle,
            @Nullable Object description) {
        static Fingerprint of(QuestObjectBase object) {
            return switch (object) {
                case Quest quest -> new Fingerprint(quest.getTitle(), quest.getSubtitle(), quest.getDescription());
                case Chapter chapter -> new Fingerprint(chapter.getTitle(), chapter.getRawSubtitle(), null);
                default -> new Fingerprint(object.getTitle(), null, null);
            };
        }

        boolean matches(Fingerprint other) {
            return same(title, other.title) && same(subtitle, other.subtitle) && same(description, other.description);
        }

        private static boolean same(@Nullable Object a, @Nullable Object b) {
            return a == b || Objects.equals(a, b);
        }
    }
}
