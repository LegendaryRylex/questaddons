package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.questaddons.client.TextIndex;
import java.util.BitSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class TextIndexTest {
    private static final TextIndex INDEX = new TextIndex.Builder()
            .add(0, "Iron Furnace")
            .add(1, "Smelt ores in a furnace made of iron.")
            .add(2, "§aGold§r, and lots of it!")
            .add(2, "Gold again")
            .reserve(4)
            .build();

    private static BitSet docs(int... ids) {
        BitSet set = new BitSet();
        for (int id : ids) {
            set.set(id);
        }
        return set;
    }

    @Test
    void splitsOnPunctuationAndLowercases() {
        assertEquals(List.of("smelt", "ores", "in", "a", "furnace"), TextIndex.tokens("Smelt ORES, in a-furnace."));
    }

    @Test
    void matchesWordPrefixes() {
        assertEquals(docs(0, 1), INDEX.matchPrefix("furn"));
    }

    @Test
    void requiresEveryWordInAnyOrder() {
        assertEquals(docs(0, 1), INDEX.matchAll(List.of("iron", "furnace")));
        assertEquals(docs(1), INDEX.matchAll(List.of("furnace", "smel")));
    }

    @Test
    void findsNothingForAnUnknownWord() {
        assertEquals(new BitSet(), INDEX.matchAll(List.of("diamond")));
    }

    @Test
    void listsADocumentOnceForRepeatedWords() {
        assertEquals(docs(2), INDEX.matchPrefix("gold"));
    }

    @Test
    void countsReservedDocuments() {
        assertEquals(4, INDEX.docCount());
    }
}
