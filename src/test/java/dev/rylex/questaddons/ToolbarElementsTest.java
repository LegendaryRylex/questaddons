package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rylex.questaddons.client.ElementInsertion;
import dev.rylex.questaddons.client.ToolbarElements;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ToolbarElementsTest {
    private static String apply(String marked, String element) {
        int start = marked.indexOf('|');
        int end = marked.lastIndexOf('|') - 1;
        String text = marked.replace("|", "");
        return ElementInsertion.insert(text, start, end, element, plain -> "PLAIN:" + plain)
                .text();
    }

    @Test
    void questLinkStylesPartOfAnExistingArrayLine() {
        String link = ToolbarElements.link("Iron", "change_page", "1A2B");
        assertEquals(
                "[ \"Smelt \", {\"text\": \"Iron\", \"underlined\": true, \"clickEvent\": {\"action\": \"change_page\", \"value\": \"1A2B\"}}, \" now\" ]",
                apply("[ \"Smelt |Iron| now\" ]", link));
    }

    @Test
    void questLinkKeepsTheStyleOfTheObjectItSplits() {
        String link = ToolbarElements.link("Iron", "change_page", "1A2B");
        assertEquals(
                "[ {\"text\": \"Smelt \", \"color\": \"gold\"}, {\"text\": \"Iron\", \"color\": \"gold\", \"underlined\": true, \"clickEvent\": {\"action\": \"change_page\", \"value\": \"1A2B\"}} ]",
                apply("{ \"text\": \"Smelt |Iron|\", \"color\": \"gold\" }", link));
    }

    @Test
    void linkTextIsEscaped() {
        assertEquals(
                "{\"text\": \"a\\\"b\", \"underlined\": true, \"clickEvent\": {\"action\": \"open_url\", \"value\": \"https://x.y\"}}",
                ToolbarElements.link("a\"b", "open_url", "https://x.y"));
    }

    @Test
    void formattingBecomesStyleKeys() {
        assertEquals(Optional.of("{\"text\": \"a\", \"bold\": true}"), ToolbarElements.styled("a", "l"));
        assertEquals(Optional.of("{\"text\": \"a\", \"strikethrough\": true}"), ToolbarElements.styled("a", "m"));
        assertEquals(Optional.of("{\"text\": \"a\", \"color\": \"dark_red\"}"), ToolbarElements.styled("a", "4"));
        assertEquals(Optional.of("{\"text\": \"a\", \"color\": \"#A1B2C3\"}"), ToolbarElements.styled("a", "#a1b2c3"));
        assertEquals(Optional.empty(), ToolbarElements.styled("a", "z"));
    }

    @Test
    void boldStylesTheSelectionInsideAJsonString() {
        String bold = ToolbarElements.styled("big", "l").orElseThrow();
        assertEquals("[ \"a \", {\"text\": \"big\", \"bold\": true}, \" word\" ]", apply("[ \"a |big| word\" ]", bold));
    }

    @Test
    void onlySingleLineSelectionsOnComponentLinesQualify() {
        String text = "plain\n[ \"json\" ]";
        int json = text.indexOf("json");
        assertTrue(ToolbarElements.onComponentLine(text, json, json + 4));
        assertFalse(ToolbarElements.onComponentLine(text, 0, 5));
        assertFalse(ToolbarElements.onComponentLine(text, 2, json));
        assertFalse(ToolbarElements.onComponentLine(text, json, json));
    }
}
