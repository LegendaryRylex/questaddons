package dev.rylex.questaddons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.questaddons.compat.ftbfiltersystem.FilterSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterSyntaxTest {

    @Test
    void orWrapsEveryItemIdWithNoSeparators() {
        assertEquals(
                "or(item(ltxi:rocket_turret)item(ltxi:arc_turret))",
                FilterSyntax.or(List.of("ltxi:rocket_turret", "ltxi:arc_turret")));
    }

    @Test
    void orKeepsASingleItemWrapped() {
        assertEquals("or(item(minecraft:stone))", FilterSyntax.or(List.of("minecraft:stone")));
    }
}
