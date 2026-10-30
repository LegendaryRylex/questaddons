package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.questaddons.client.KeybindInsertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KeybindInsertionTest {
    private static final String KEY = KeybindInsertion.element("key.jump", "Space");

    private static KeybindInsertion.Result insert(String marked) {
        int start = marked.indexOf('|');
        int end = marked.lastIndexOf('|');
        String text = marked.replace("|", "");
        return KeybindInsertion.insert(text, start, end == start ? start : end - 1, KEY, plain -> "PLAIN:" + plain);
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
                "{\"keybind\": \"key.jei.showRecipe\", \"color\": \"yellow\", \"hover_event\": {\"action\": \"show_text\", \"value\": \"Default: R\"}}",
                KeybindInsertion.element("key.jei.showRecipe", "R"));
    }

    @Test
    void cursorLandsAfterTheElement() {
        KeybindInsertion.Result result = insert("[ \"a|b\" ]");
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
                "PLAIN:&aPress " + KeybindInsertion.MARKER + " now",
                insert("&aPress | now").text());
        assertEquals(
                "PLAIN:Press " + KeybindInsertion.MARKER + " now",
                insert("Press |to| now").text());
    }

    @Test
    void nonComponentBraceLinesKeepTheirLine() {
        assertEquals("{image:a:b.png}\n" + KEY, insert("{ima|ge:a:b.png}").text());
        assertEquals(
                "{@pagebreak}\n" + KEY + "\nnext", insert("{@page|break}\nnext").text());
    }

    @Test
    void selectionAcrossLinesIsKept() {
        assertEquals(
                "PLAIN:o" + KeybindInsertion.MARKER + "ne\ntwo",
                insert("o|ne\ntw|o").text().replace("twoo", "two"));
    }
}
