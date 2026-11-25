package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.questaddons.client.ElementInsertion;
import dev.rylex.questaddons.client.ImageInsertion;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

class ImageInsertionTest {
    private static final String TAG = ImageInsertion.tag("minecraft:textures/item/diamond.png", 8, 8, null);
    private static final String SIDE = ImageInsertion.tag("minecraft:textures/item/diamond.png", 32, 32, "left");
    private static final String JSON_TAG = "{ \"text\": \"" + TAG + "\" }";

    private static ElementInsertion.Result insert(String marked, String tag, boolean atLineStart) {
        return insert(marked, tag, atLineStart, UnaryOperator.identity());
    }

    private static ElementInsertion.Result insert(
            String marked, String tag, boolean atLineStart, UnaryOperator<String> fixJson) {
        int cursor = marked.indexOf('|');
        return ImageInsertion.insert(marked.replace("|", ""), cursor, tag, atLineStart, "EDIT HERE", fixJson);
    }

    @Test
    void tagCarriesSizeAndOptionalAlign() {
        assertEquals("{image:minecraft:textures/item/diamond.png width:8 height:8}", TAG);
        assertEquals("{image:minecraft:textures/item/diamond.png width:32 height:32 align:left}", SIDE);
    }

    @Test
    void tagWritesValignOnlyWhenNotTop() {
        assertEquals(SIDE, ImageInsertion.tag("minecraft:textures/item/diamond.png", 32, 32, "left", "top"));
        assertEquals(
                "{image:item:minecraft:beacon width:32 height:32 align:right valign:bottom}",
                ImageInsertion.tag("item:minecraft:beacon", 32, 32, "right", "bottom"));
    }

    @Test
    void inlineGoesAtTheCursorOfAPlainLine() {
        ElementInsertion.Result result = insert("Mine |diamonds", TAG, false);
        assertEquals("Mine " + TAG + "diamonds", result.text());
        assertEquals(("Mine " + TAG).length(), result.cursor());
    }

    @Test
    void sideImageGoesAtTheStartOfAPlainLine() {
        ElementInsertion.Result result = insert("first\nSome |text", SIDE, true);
        assertEquals("first\n" + SIDE + " Some text", result.text());
    }

    @Test
    void sideImageOnAnEmptyLineGetsPlaceholderText() {
        assertEquals(SIDE + " EDIT HERE", insert("|", SIDE, true).text());
        assertEquals(TAG, insert("|", TAG, false).text());
    }

    @Test
    void jsonArrayGetsAnImageTextElement() {
        ElementInsertion.Result result = insert("[ \"Mine |diamonds\" ]", TAG, false);
        assertEquals("[ \"Mine \", " + JSON_TAG + ", \"diamonds\" ]", result.text());
        assertEquals(("[ \"Mine \", " + JSON_TAG).length(), result.cursor());
    }

    @Test
    void jsonObjectBecomesAnArray() {
        assertEquals(
                "[ { \"text\": \"a\" }, " + JSON_TAG + " ]",
                insert("{ \"text\": \"a|\" }", TAG, false).text());
    }

    @Test
    void jsonLineIsFixedAfterTheInsert() {
        String text = insert("before\n  [ \"a|\" ]\nafter", TAG, false, line -> "FIXED " + line)
                .text();
        assertEquals("before\n  FIXED [ \"a\", " + JSON_TAG + " ]\nafter", text);
    }
}
