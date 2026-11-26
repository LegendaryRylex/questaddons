package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rylex.questaddons.client.ComponentJsonRepair;
import dev.rylex.questaddons.client.LenientJson;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class JsonRepairTest {

    private static String repair(String line) {
        return LenientJson.parse(line)
                .map(ComponentJsonRepair::repairLine)
                .map(Object::toString)
                .orElse(null);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"text\":\"a\"}",
                "[\"\",{\"text\":\"a\"}]",
                "{ 'text': 'a' }",
                "{text:\"a\"}",
                "[{\"text\":\"a\"}]",
                "{\u201ctext\u201d:\u201ca\u201d}"
            })
    void recognisesComponentLines(String line) {
        assertTrue(ComponentJsonRepair.looksLikeComponent(line));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{@pagebreak}",
                "{image:ftbquests:textures/foo.png width:100 height:100}",
                "{open_url:https://example.com text:Click}",
                "{some.translation.key}",
                "[Tip] Plain text",
                "&6Plain text",
                "[]"
            })
    void leavesFtbTagsAndPlainTextAlone(String line) {
        assertFalse(ComponentJsonRepair.looksLikeComponent(line));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            quoteCharacter = '`',
            value = {
                "{\"text\":\"a\",}|{\"text\":\"a\"}",
                "[\"a\",{\"text\":\"b\"},]|[\"a\",{\"text\":\"b\"}]",
                "{\"text\":\"a\"|{\"text\":\"a\"}",
                "[{\"text\":\"a\"}|[{\"text\":\"a\"}]",
                "{\"text\":\"a}|{\"text\":\"a\"}",
                "{\"text\":\"a\" \"color\":\"red\"}|{\"text\":\"a\",\"color\":\"red\"}",
                "{\u201ctext\u201d: \u201cIt\u2019s here\u201d}|{\"text\":\"It\u2019s here\"}",
                "{'text': 'It\u2019s here'}|{\"text\":\"It\u2019s here\"}",
                "{\"text\":\"He said \"hi\" to me\"}|{\"text\":\"He said \\\"hi\\\" to me\"}",
                "{\"text\":\"a\"}}|{\"text\":\"a\"}",
                "{\"text\":\"a\"},{\"text\":\"b\"}|[\"\",{\"text\":\"a\"},{\"text\":\"b\"}]",
                "{text: Hello world, color: gold}|{\"text\":\"Hello world\",\"color\":\"gold\"}",
                "{\"text\":\"a\\'b\"}|{\"text\":\"a'b\"}"
            })
    void repairsSyntax(String line, String expected) {
        assertEquals(expected, repair(line));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            quoteCharacter = '`',
            value = {
                "{\"text\":\"a\",\"color\":\"Gold\"}|{\"text\":\"a\",\"color\":\"gold\"}",
                "{\"text\":\"a\",\"color\":\"dark grey\"}|{\"text\":\"a\",\"color\":\"dark_gray\"}",
                "{\"text\":\"a\",\"color\":\"ffaa00\"}|{\"text\":\"a\",\"color\":\"#FFAA00\"}",
                "{\"text\":\"a\",\"color\":\"#fa0\"}|{\"text\":\"a\",\"color\":\"#FFAA00\"}",
                "{\"text\":\"a\",\"color\":\"#ffaa00\"}|{\"text\":\"a\",\"color\":\"#ffaa00\"}",
                "{\"text\":\"a\",\"bold\":\"true\"}|{\"text\":\"a\",\"bold\":true}",
                "{\"text\":\"a\",\"underline\":true}|{\"text\":\"a\",\"underlined\":true}",
                "{\"Text\":\"a\",\"Color\":\"red\"}|{\"text\":\"a\",\"color\":\"red\"}",
                "{\"color\":\"red\",\"extra\":[\"a\"]}|{\"text\":\"\",\"color\":\"red\",\"extra\":[\"a\"]}",
                "{\"text\":5}|{\"text\":\"5\"}",
                "{\"text\":\"a\",\"extra\":[]}|{\"text\":\"a\"}",
                "{\"text\":\"a\",\"extra\":{\"text\":\"b\"}}|{\"text\":\"a\",\"extra\":[{\"text\":\"b\"}]}"
            })
    void repairsStyle(String line, String expected) {
        assertEquals(expected, repair(line));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            quoteCharacter = '`',
            value = {
                "{\"text\":\"a\",\"click_event\":{\"action\":\"open_url\",\"url\":\"https://x.y\"}}"
                        + "|{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}}",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"RUN_COMMAND\",\"command\":\"/say hi\"}}"
                        + "|{\"text\":\"a\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/say hi\"}}",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"change_page\",\"value\":3}}"
                        + "|{\"text\":\"a\",\"clickEvent\":{\"action\":\"change_page\",\"value\":\"3\"}}",
                "{\"text\":\"a\",\"hover_event\":{\"action\":\"show_text\",\"value\":\"tip\"}}"
                        + "|{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"tip\"}}",
                "{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_item\",\"id\":\"minecraft:stone\",\"count\":2}}"
                        + "|{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":2}}}"
            })
    void migratesEventsToThisVersion(String line, String expected) {
        assertEquals(expected, repair(line));
    }

    @Test
    void keepsTheCanonicalKeyWhenBothSpellingsArePresent() {
        assertEquals(
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"x\"}}",
                repair("{\"text\":\"a\",\"click_event\":{\"action\":\"open_url\",\"url\":\"y\"},"
                        + "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"x\"}}"));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            quoteCharacter = '`',
            value = {
                "[\"Mine it with &dSilk Touch&r from your \",{\"text\":\"Drill\",\"color\":\"green\"},\".\"]"
                        + "|[\"Mine it with \",{\"text\":\"Silk Touch\",\"color\":\"light_purple\"},"
                        + "\" from your \",{\"text\":\"Drill\",\"color\":\"green\"},\".\"]",
                "[\"&aGreen&r plain\",\"b\"]|[\"\",{\"text\":\"Green\",\"color\":\"green\"},\" plain\",\"b\"]",
                "[\"\u00a7lBold\"]|[\"\",{\"text\":\"Bold\",\"bold\":true}]",
                "[\"&#ff8800Orange &lloud\"]|[\"\",{\"text\":\"Orange \",\"color\":\"#FF8800\"},"
                        + "{\"text\":\"loud\",\"color\":\"#FF8800\",\"bold\":true}]",
                "{\"text\":\"Hi &cthere\",\"italic\":true}"
                        + "|{\"text\":\"Hi \",\"italic\":true,\"extra\":[{\"text\":\"there\",\"color\":\"red\"}]}",
                "{\"text\":\"a\",\"extra\":[\"&cred\"]}|{\"text\":\"a\",\"extra\":[{\"text\":\"red\",\"color\":\"red\"}]}",
                "{\"text\":\"&aStone\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}"
                        + "|{\"text\":\"Stone\",\"color\":\"green\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}",
                "{\"text\":\"&aStone\",\"color\":\"red\",\"bold\":true}|{\"text\":\"Stone\",\"color\":\"green\",\"bold\":true}",
                "{\"text\":\"&aStone\",\"extra\":[\"b\"]}|{\"text\":\"\",\"extra\":[{\"text\":\"Stone\",\"color\":\"green\"},\"b\"]}",
                "{\"text\":\"&aStone &lloud\"}|{\"text\":\"\",\"extra\":[{\"text\":\"Stone \",\"color\":\"green\"},"
                        + "{\"text\":\"loud\",\"color\":\"green\",\"bold\":true}]}",
                "[\"R&D & more\"]|[\"R&D & more\"]"
            })
    void convertsFormattingCodes(String line, String expected) {
        assertEquals(expected, repair(line));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            quoteCharacter = '`',
            value = {
                "[{\"text\":\"{image:a.png}\"},{\"text\":\"{\\\"text\\\": \\\"{image:a.png}\",\"underlined\":true},\"b\"]|[{\"text\":\"{image:a.png}\",\"underlined\":true},\"b\"]",
                "[\"a\",{\"text\":\"{\\\"text\\\": \\\"b\\\"}\",\"bold\":true}]|[\"a\",{\"text\":\"b\",\"bold\":true}]",
                "{\"text\":\"{\\\"text\\\": \\\"say \\\\\\\"hi\\\\\\\"\",\"bold\":true}|{\"text\":\"say \\\"hi\\\"\",\"bold\":true}"
            })
    void unwrapsTextThatSwallowedItsOwnElement(String line, String expected) {
        assertEquals(expected, repair(line));
    }

    @Test
    void mergesALinkAndAnItemTooltipSplitAcrossTwoCopiesOfTheSameText() {
        assertEquals(
                "[{\"translate\":\"item.oritech.raw_biopolymer\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"jei:modern_industrialization:electric_mixer?recipe=ftb:oritech/mi_mixer/raw_biopolymer_packed_wheat\"},\"underlined\":true,\"hoverEvent\":{\"contents\":{\"id\":\"oritech:raw_biopolymer\",\"count\":1},\"action\":\"show_item\"}}]",
                repair(
                        "[{\"translate\": \"item.oritech.raw_biopolymer\", \"hoverEvent\": {\"contents\": \"\", \"action\": \"show_text\"}, \"clickEvent\": {\"action\": \"open_url\", \"value\": \"jei:modern_industrialization:electric_mixer?recipe=ftb:oritech/mi_mixer/raw_biopolymer_packed_wheat\"}, \"underlined\": true}, {\"translate\": \"item.oritech.raw_biopolymer\", \"hoverEvent\": {\"contents\": {\"id\": \"oritech:raw_biopolymer\", \"count\": 1}, \"action\": \"show_item\"}}]"));
    }

    @Test
    void dropsEmptyHoverText() {
        assertEquals(
                "{\"text\":\"a\"}",
                repair("{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"\"}}"));
        assertEquals(
                "{\"text\":\"a\"}",
                repair(
                        "{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"\",\"bold\":true}}}"));
    }

    @Test
    void keepsSplitEventsOnDifferentText() {
        assertEquals(
                "[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"x\"}},{\"text\":\"b\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"t\"}}]",
                repair(
                        "[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"x\"}},{\"text\":\"b\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"t\"}}]"));
        assertEquals(
                "[{\"text\":\"a\",\"bold\":true},{\"text\":\"a\"}]",
                repair("[{\"text\":\"a\",\"bold\":true},{\"text\":\"a\"}]"));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            quoteCharacter = '`',
            value = {
                "[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"t\"}},{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]|[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"},\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]",
                "[{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"t\"}},{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"u\"}}]|[{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"t\"}},{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"u\"}}]",
                "[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}},{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}}]|[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}}]",
                "[{\"text\":\"a\",\"color\":\"gold\"},{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]|[{\"text\":\"a\",\"color\":\"gold\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]",
                "[{\"text\":\"a\",\"insertion\":\"x\"},{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}}]|[{\"text\":\"a\",\"insertion\":\"x\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}}]",
                "[\"b\",{\"text\":\"\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}},\"a\"]|[\"b\",{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}}]",
                "[{\"text\":\"\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}},\"a\"]|[{\"text\":\"\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}},\"a\"]",
                "[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}},{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}},\"rest\"]|[{\"text\":\"\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}},{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"},\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}},\"rest\"]",
                "{\"text\":\"x\",\"extra\":[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}},{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]}|{\"text\":\"x\",\"extra\":[{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"},\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]}",
                "[{\"translate\":\"item.x\"},{\"text\":\"Stone\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]|[{\"translate\":\"item.x\"},{\"text\":\"Stone\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"\"}}|{\"text\":\"a\"}",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"copy_to_clipboard\",\"value\":\"\"}}|{\"text\":\"a\"}",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"change_page\",\"value\":\"\"}}|{\"text\":\"a\"}",
                "{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:air\"}}}|{\"text\":\"a\"}",
                "{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":{\"name\":\"Bob\"}}}|{\"text\":\"a\"}",
                "{\"text\":\"a\",\"insertion\":\"\"}|{\"text\":\"a\"}"
            })
    void mergesSplitEventsAndDropsDeadOnes(String line, String expected) {
        assertEquals(expected, repair(line));
    }

    @Test
    void mergesATranslatedNameWithItsLiteralCopy() {
        assertEquals(
                "[{\"translate\":\"item.x\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]",
                LenientJson.parse(
                                "[{\"translate\":\"item.x\"},{\"text\":\"Stone\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":1}}}]")
                        .map(json -> ComponentJsonRepair.repairLine(
                                json, key -> key.equals("item.x") ? Optional.of("Stone") : Optional.empty()))
                        .map(Object::toString)
                        .orElse(null));
    }

    @Test
    void rejectsUnrecoverableSyntax() {
        assertTrue(LenientJson.parse("{\"text\":}").isEmpty());
        assertTrue(LenientJson.parse("{\"text\" \"a\"}").isEmpty());
    }
}
