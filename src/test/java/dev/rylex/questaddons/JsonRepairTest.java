package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rylex.questaddons.client.ComponentJsonRepair;
import dev.rylex.questaddons.client.LenientJson;
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
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://x.y\"}}"
                        + "|{\"text\":\"a\",\"click_event\":{\"action\":\"open_url\",\"url\":\"https://x.y\"}}",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"RUN_COMMAND\",\"value\":\"/say hi\"}}"
                        + "|{\"text\":\"a\",\"click_event\":{\"action\":\"run_command\",\"command\":\"/say hi\"}}",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"change_page\",\"value\":\"3\"}}"
                        + "|{\"text\":\"a\",\"click_event\":{\"action\":\"change_page\",\"page\":3}}",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"change_page\",\"value\":\"1A2B3C4D5E6F7A8B\"}}"
                        + "|{\"text\":\"a\",\"click_event\":{\"action\":\"custom\",\"id\":\"ftbquests:link\","
                        + "\"payload\":{\"quest_id\":\"1A2B3C4D5E6F7A8B\",\"page\":1}}}",
                "{\"text\":\"a\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"docs:ftbquests/intro\"}}"
                        + "|{\"text\":\"a\",\"click_event\":{\"action\":\"custom\",\"id\":\"ftbquests:docs\","
                        + "\"payload\":{\"location\":\"ftbquests/intro\"}}}",
                "{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"tip\"}}"
                        + "|{\"text\":\"a\",\"hover_event\":{\"action\":\"show_text\",\"value\":\"tip\"}}",
                "{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_item\",\"contents\":{\"id\":\"minecraft:stone\",\"count\":2}}}"
                        + "|{\"text\":\"a\",\"hover_event\":{\"action\":\"show_item\",\"id\":\"minecraft:stone\",\"count\":2}}",
                "{\"text\":\"a\",\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":{\"type\":\"minecraft:pig\",\"name\":\"Bob\"}}}"
                        + "|{\"text\":\"a\",\"hover_event\":{\"action\":\"show_entity\",\"id\":\"minecraft:pig\",\"name\":\"Bob\"}}"
            })
    void migratesEventsToThisVersion(String line, String expected) {
        assertEquals(expected, repair(line));
    }

    @Test
    void keepsTheCanonicalKeyWhenBothSpellingsArePresent() {
        assertEquals(
                "{\"text\":\"a\",\"click_event\":{\"action\":\"open_url\",\"url\":\"y\"}}",
                repair("{\"text\":\"a\",\"click_event\":{\"action\":\"open_url\",\"url\":\"y\"},"
                        + "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"x\"}}"));
    }

    @Test
    void rejectsUnrecoverableSyntax() {
        assertTrue(LenientJson.parse("{\"text\":}").isEmpty());
        assertTrue(LenientJson.parse("{\"text\" \"a\"}").isEmpty());
    }
}
