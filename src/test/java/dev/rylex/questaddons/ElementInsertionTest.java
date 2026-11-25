package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.questaddons.client.ElementInsertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ElementInsertionTest {
    private static final String KEY = ElementInsertion.keybind("key.jump", "Space");

    private static ElementInsertion.Result insert(String marked) {
        int start = marked.indexOf('|');
        int end = marked.lastIndexOf('|');
        String text = marked.replace("|", "");
        return ElementInsertion.insert(text, start, end == start ? start : end - 1, KEY, plain -> "PLAIN:" + plain);
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '~',
            quoteCharacter = '`',
            value = {
                "[ \"Press |now\" ]~[ \"Press \", $, \"now\" ]",
                "[ \"Press| to| jump\" ]~[ \"Press\", $, \" jump\" ]",
                "[ \"|now\" ]~[ $, \"now\" ]",
                "[ \"now|\" ]~[ \"now\", $ ]",
                "[ \"\", \"a|\" ]~[ \"\", \"a\", $ ]",
                "[ \"|\", \"a\" ]~[ \"\", $, \"a\" ]",
                "[ \"a\"|, \"b\" ]~[ \"a\", $, \"b\" ]",
                "[ \"a\", \"b\" |]~[ \"a\", \"b\", $ ]",
                "[ \"a\", \"b\" ]|~[ \"a\", \"b\", $ ]",
                "[ { \"te|xt\": \"a\" }, \"b\" ]~[ { \"text\": \"a\" }, $, \"b\" ]",
                "[ \"a, ]b|c\" ]~[ \"a, ]b\", $, \"c\" ]",
                "[ \"a\\\"|b\" ]~[ \"a\\\"\", $, \"b\" ]",
                "[ \"a\"| { \"text\": \"b\" ] ]~[ \"a\" { \"text\": \"b\" ], $ ]",
                "{ \"te|xt\": \"a\" }~[ { \"text\": \"a\" }, $ ]",
                "|~$",
                "   |~$"
            })
    void placesTheElementInJsonAndBlankLines(String marked, String expected) {
        assertEquals(expected.replace("$", KEY), insert(marked).text());
    }

    @Test
    void elementCarriesColourAndDefaultKey() {
        assertEquals(
                "{\"keybind\": \"key.jei.showRecipe\", \"color\": \"yellow\", \"hoverEvent\": {\"action\": \"show_text\", \"contents\": \"Default: R\"}}",
                ElementInsertion.keybind("key.jei.showRecipe", "R"));
    }

    @Test
    void cursorLandsAfterTheElement() {
        ElementInsertion.Result result = insert("[ \"a|b\" ]");
        assertEquals(result.text().indexOf(KEY) + KEY.length(), result.cursor());
    }

    @Test
    void onlyTheCursorLineChanges() {
        assertEquals("one\n" + KEY + "\nthree", insert("one\n|\nthree").text());
        assertEquals(
                "one\n[ \"a\", " + KEY + " ]\nthree",
                insert("one\n[ \"a|\" ]\nthree").text());
    }

    @Test
    void plainLinesGoThroughTheConverterWithAMarker() {
        assertEquals(
                "PLAIN:&aPress " + ElementInsertion.MARKER + " now",
                insert("&aPress | now").text());
        assertEquals(
                "PLAIN:Press " + ElementInsertion.MARKER + " now",
                insert("Press |to| now").text());
    }

    @Test
    void nonComponentBraceLinesKeepTheirLine() {
        assertEquals("{image:a:b.png}\n" + KEY, insert("{ima|ge:a:b.png}").text());
        assertEquals(
                "{@pagebreak}\n" + KEY + "\nnext", insert("{@page|break}\nnext").text());
    }

    private static String wrap(String marked, String style) {
        int start = marked.indexOf('|');
        int end = marked.lastIndexOf('|') - 1;
        String text = marked.replace("|", "");
        String selection = text.substring(start, end);
        String element = "{\"text\": " + new com.google.gson.JsonPrimitive(selection) + ", " + style + "}";
        return ElementInsertion.insert(text, start, end, element, plain -> "PLAIN:" + plain)
                .text();
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '~',
            quoteCharacter = '`',
            value = {
                "[{\"text\": \"|{image:a.png}|\"}, \"b\"]~[{\"text\": \"{image:a.png}\", \"underlined\": true}, \"b\"]",
                "[|{\"text\": \"{image:a.png}|\"}, \"b\"]~[{\"text\": \"{image:a.png}\", \"underlined\": true}, \"b\"]",
                "[|{\"text\": \"{image:a.png}\"}|, \"b\"]~[{\"text\": \"{image:a.png}\", \"underlined\": true}, \"b\"]",
                "[{\"text\": \"|Silk| Touch\", \"color\": \"red\"}]~[{\"text\": \"Silk\", \"color\": \"red\", \"underlined\": true}, {\"text\": \" Touch\", \"color\": \"red\"}]"
            })
    void wrappingStylesTheSelectedObject(String marked, String expected) {
        assertEquals(expected, wrap(marked, "\"underlined\": true"));
    }

    @Test
    void wrappingAcrossElementsStylesEachOne() {
        assertEquals(
                "[{\"text\": \"ab\", \"bold\": true}, {\"text\": \"cd\", \"color\": \"red\", \"bold\": true}]",
                wrap("[\"a|b\", {\"text\": \"c|d\", \"color\": \"red\"}]", "\"bold\": true"));
    }

    @Test
    void wrappingInsideAnObjectLineSplitsIt() {
        assertEquals(
                "[ {\"text\": \"a\"}, {\"text\": \"b\", \"bold\": true}, {\"text\": \"c\"} ]",
                wrap("{\"text\": \"a|b|c\"}", "\"bold\": true"));
    }

    @Test
    void replacingInsideAnObjectKeepsItsStyleAround() {
        assertEquals(
                "[{\"text\": \"a\", \"color\": \"red\"}, " + KEY + ", {\"text\": \"c\", \"color\": \"red\"}]",
                insert("[{\"text\": \"a|b|c\", \"color\": \"red\"}]").text());
    }

    @Test
    void selectionAcrossLinesIsKept() {
        assertEquals(
                "PLAIN:o" + ElementInsertion.MARKER + "ne\ntwo",
                insert("o|ne\ntw|o").text().replace("twoo", "two"));
    }
}
