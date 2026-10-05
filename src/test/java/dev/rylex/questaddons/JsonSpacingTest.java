package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.questaddons.client.JsonSpacing;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JsonSpacingTest {

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            quoteCharacter = '`',
            value = {
                "{\"text\":\"a\"}|{ \"text\": \"a\" }",
                "{\"text\":\"a\",\"color\":\"red\"}|{ \"text\": \"a\", \"color\": \"red\" }",
                "[\"\",{\"text\":\"a\"},{\"text\":\"b\"}]|[ \"\", { \"text\": \"a\" }, { \"text\": \"b\" } ]",
                "{\"text\":\"a\",\"bold\":true}|{ \"text\": \"a\", \"bold\": true }",
                "{\"text\":\"a\",\"with\":[]}|{ \"text\": \"a\", \"with\": [] }",
                "{\"text\":\"a\",\"extra\":[ ]}|{ \"text\": \"a\", \"extra\": [] }",
                "{   \"text\"  :  \"a\"  ,\"color\":\"red\"}|{ \"text\": \"a\", \"color\": \"red\" }",
                "{\"text\":\"a,b:c {d} [e]\"}|{ \"text\": \"a,b:c {d} [e]\" }",
                "{\"text\":\"say \\\"hi\\\", ok\"}|{ \"text\": \"say \\\"hi\\\", ok\" }",
                "{\"text\":\"\\u00a76a\"}|{ \"text\": \"\\u00a76a\" }",
                "{\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://a.b/c\"}}"
                        + "|{ \"clickEvent\": { \"action\": \"open_url\", \"value\": \"https://a.b/c\" } }"
            })
    void spacesEveryMember(String line, String expected) {
        assertEquals(expected, JsonSpacing.spaced(line));
        assertEquals(expected, JsonSpacing.spaced(expected));
    }
}
